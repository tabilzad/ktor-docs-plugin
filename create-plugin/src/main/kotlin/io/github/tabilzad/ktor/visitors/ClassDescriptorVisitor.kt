
import arrow.meta.phases.resolve.unwrappedNotNullableType
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import io.github.tabilzad.ktor.forEachVariable
import io.github.tabilzad.ktor.names
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.isAny
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.util.removeSuffixIfPresent

class ClassDescriptorVisitor(val context: BindingContext) :
    DeclarationDescriptorVisitorEmptyBodies<ObjectType, ObjectType>() {
    val classNames = mutableListOf<ObjectType>()

    override fun visitClassDescriptor(descriptor: ClassDescriptor?, parent: ObjectType): ObjectType {
        val type = descriptor?.defaultType!!
        return if (KotlinBuiltIns.isPrimitiveType(type.unwrappedNotNullableType) || KotlinBuiltIns.isString(type.unwrappedNotNullableType)) {
            parent.type = descriptor.name.asString().toSwaggerType()
            parent.properties = null
            parent
        } else if (KotlinBuiltIns.isListOrNullableList(type)) {

            DescriptorUtils.getClassDescriptorForType(type).let { descr ->
                parent.type = "array"
                parent.items = ObjectType("object", mutableMapOf())
            }
            parent
        } else {
            parent.type = "object"
            val jetTypeFqName = type.getJetTypeFqName(false)
            parent.name = jetTypeFqName
            if (!classNames.names.contains(jetTypeFqName)) {
                classNames.add(parent)
                type.memberScope
                    .forEachVariable { nestedDescr: DeclarationDescriptor ->
                        nestedDescr.accept(this, parent)
                    }
            }
            parent.apply {
                ref = "#/definitions/$jetTypeFqName"
            }

            parent
        }
    }

    override fun visitPropertyDescriptor(
        descriptor: PropertyDescriptor,
        parent: ObjectType
    ): ObjectType {

        ForceResolveUtil.forceResolveAllContents(descriptor.annotations)
        val type = descriptor.type
        val propertyName = descriptor.resolvePropertyName()

        return when {
            KotlinBuiltIns.isPrimitiveType(type.unwrappedNotNullableType) || KotlinBuiltIns.isString(type.unwrappedNotNullableType) -> {
                var result = parent
                if (parent.type == "object") {
                    parent.properties?.put(
                        propertyName,
                        ObjectType(type.toString().toSwaggerType(), mutableMapOf())
                    )
                }
                if (parent.type == "array") {
                    val objectType = ObjectType(
                        "object",
                        mutableMapOf(propertyName to ObjectType(type.toString().toSwaggerType()))
                    )
                    result = objectType
                }
                result
            }
            else -> {
                val name = type.getJetTypeFqName(true)
                when {
                    type.isIterable() || type.isArrayOrNullableArray() -> {

                        val types = type.unfoldNestedParameters().reversed().map {
                            DescriptorUtils.getClassDescriptorForType(it.type)
                        }
                        parent.properties?.put(
                            propertyName,
                            types.fold(ObjectType("object", mutableMapOf())) { acc: ObjectType, d: ClassDescriptor ->
                                val classType = d.defaultType
                                var t = acc
                                while (t.items != null) {
                                    t = t.items!!
                                }
                                if (KotlinBuiltIns.isPrimitiveType(classType) || KotlinBuiltIns.isString(classType)) {
                                    t.type = d.name.asString().toSwaggerType()
                                    t.properties = null
                                } else if (classType.isIterable() || classType.isArrayOrNullableArray()) {
                                    t.type = "array"
                                    t.items = ObjectType("object", mutableMapOf())
                                } else if (classType.isEnum()) {
                                    t.type = "string"
                                    t.enum =
                                        classType.memberScope.getContributedDescriptors(DescriptorKindFilter.VALUES)
                                            .map { it.name.asString() }
                                            .filterNot {
                                                listOf(
                                                    "name",
                                                    "ordinal",
                                                    "clone",
                                                    "compareTo",
                                                    "describeConstable",
                                                    "equals",
                                                    "finalize",
                                                    "getDeclaringClass",
                                                    "hashCode",
                                                    "toString"
                                                ).contains(it)
                                            }
                                } else {
                                    t.type = "object"
                                    val jetTypeFqName = classType.getJetTypeFqName(false)
                                    if (!classNames.names.contains(jetTypeFqName)) {

                                        val q = ObjectType(
                                            "object",
                                            mutableMapOf(),
                                            name = jetTypeFqName,
                                        )
                                        classNames.add(q)
                                        classType.memberScope
                                            .forEachVariable { nestedDescr: DeclarationDescriptor ->
                                                nestedDescr.accept(this, q)
                                            }
                                    }
                                    t.apply<ObjectType> {
                                        ref = "#/definitions/$jetTypeFqName"
                                        properties = null
                                    }
                                }
                                acc
                            }
                        )

                        parent
                    }

                    type.isEnum() -> {
                        val enum =
                            type.memberScope.getContributedDescriptors(DescriptorKindFilter.VALUES)
                                .map { it.name.asString() }
                                .filterNot {
                                    listOf(
                                        "name",
                                        "ordinal",
                                        "clone",
                                        "compareTo",
                                        "describeConstable",
                                        "equals",
                                        "finalize",
                                        "getDeclaringClass",
                                        "hashCode",
                                        "toString"
                                    ).contains(it)
                                }

                        parent.properties?.put(
                            propertyName, ObjectType(
                                "string", enum =
                                enum
                            )
                        )
                        parent
                    }

                    type.isMap() -> {
                        val types = type.arguments.map {
                            DescriptorUtils.getClassDescriptorForType(it.type)
                        }
                        parent.properties?.put(
                            propertyName,
                            types.fold(ObjectType("object", mutableMapOf())) { acc, d ->
                                val valueClassType = d.defaultType
                                when {
                                    KotlinBuiltIns.isPrimitiveType(valueClassType) || KotlinBuiltIns.isString(
                                        valueClassType
                                    ) -> {
                                        acc.additionalProperties = ObjectType(d.name.asString().toSwaggerType())
                                    }

                                    valueClassType.isIterable() || valueClassType.isArrayOrNullableArray() -> {
                                        acc.type = "array"
                                        acc.items = ObjectType("object", mutableMapOf())
                                        parent.properties?.put(propertyName, acc)
                                    }

                                    valueClassType.isEnum() -> {
                                        acc.type = "string"
                                        acc.enum =
                                            valueClassType.memberScope.getClassifierNames()?.map { it.asString() }
                                                ?.minus("Companion")
                                        parent.properties?.put(propertyName, acc)
                                    }

                                    KotlinBuiltIns.isAny(valueClassType) -> {
                                        acc.type = "object"
                                    }

                                    else -> {
                                        val gName = valueClassType.getJetTypeFqName(false)
                                        if (!classNames.names.contains(gName)) {
                                            val q = ObjectType(
                                                "object",
                                                mutableMapOf(),
                                                name = gName,
                                            )
                                            classNames.add(q)

                                            valueClassType.memberScope
                                                .forEachVariable { nestedDescr: DeclarationDescriptor ->
                                                    nestedDescr.accept(this, q)
                                                }
                                        }
                                        acc.additionalProperties = ObjectType(
                                            "object",
                                            ref = "#/definitions/$gName"
                                        )
                                    }
                                }
                                acc
                            })
                        parent
                    }

                    type.isAny() -> {
                        parent
                    }

                    else -> {
                        if (!classNames.names.contains(name)) {
                            val internal = ObjectType(
                                "object",
                                mutableMapOf(),
                                name = name
                            )
                            classNames.add(internal)
                            type.memberScope
                                .forEachVariable { nestedDescr ->
                                    nestedDescr.accept(this, internal)
                                }
                        }

                        parent.properties?.put(
                            descriptor.name.asString(),
                            ObjectType(
                                "object",
                                name = name,
                                ref = "#/definitions/$name"
                            )
                        )
                        parent
                    }
                }
            }
        }
    }

    private fun PropertyDescriptor.resolvePropertyName(): String =
        annotations
            .flatMap { it.source.getPsi()?.children?.toList() ?: emptyList() }
            .find { it.text.contains("name") }
            ?.children?.firstOrNull()?.children?.last()?.text?.replace("\"", "") ?: name.asString()
}

fun KotlinType.isIterable(): Boolean {
    return supertypes().map { it.getJetTypeFqName(false) }
        .contains(DefaultBuiltIns.Instance.iterableType.getJetTypeFqName(false))
}

fun KotlinType.isMap(): Boolean {
    val jetTypeFqName = getJetTypeFqName(false)
    return listOf(
        DefaultBuiltIns.Instance.map.defaultType.getJetTypeFqName(false),
        DefaultBuiltIns.Instance.mutableMap.defaultType.getJetTypeFqName(false)
    ).any { it == jetTypeFqName }
}

fun KotlinType.unfoldNestedParameters(params: List<TypeProjection> = this.arguments): List<TypeProjection> {
    return arguments.flatMap {
        it.type.unfoldNestedParameters()
    }.plus(asTypeProjection())
}

fun String.toSwaggerType(): String {
    return when (val type = this.lowercase().removeSuffixIfPresent("?")) {
        "int" -> "integer"
        "double" -> "number"
        "float" -> "number"
        "long" -> "integer"
        else -> type
    }
}
