package io.github.tabilzad.ktor.visitors

import arrow.meta.phases.resolve.unwrappedNotNullableType
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import io.github.tabilzad.ktor.names
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.isAny
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.supertypes

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
                //parent.properties = null
                parent.items = ObjectType("object", mutableMapOf())
            }
            parent
        } else {
            parent.type = "object"
            val jetTypeFqName = type.getJetTypeFqName(false)
            if (!classNames.names.contains(jetTypeFqName)) {
                parent.name = jetTypeFqName
                classNames.add(parent)
                type.memberScope
                    .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                    .forEach { nestedDescr: DeclarationDescriptor ->
                        nestedDescr.accept(this, parent)
                    }
            }
            parent
        }
    }

    override fun visitPropertyDescriptor(
        descriptor: PropertyDescriptor,
        parent: ObjectType
    ): ObjectType {

        ForceResolveUtil.forceResolveAllContents(descriptor.annotations);
        val type = descriptor.type
        val propertyName = descriptor.resolvePropertyName()

        val h = when {
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
                                    //parent.properties = null
                                    t.items = ObjectType("object", mutableMapOf())
                                } else if (classType.isEnum()) {
                                    t.type = "string"
                                    t.enum = classType.memberScope.getClassifierNames()?.map { it.asString() }?.minus("Companion")
                                } else {
                                    t.type = "object"
                                    val jetTypeFqName = classType.getJetTypeFqName(false)
                                    if (!classNames.names.contains(jetTypeFqName)) {
                                        t.name = jetTypeFqName
                                        classNames.add(t)
                                        classType.memberScope
                                            .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                                            .forEach { nestedDescr: DeclarationDescriptor ->
                                                nestedDescr.accept(this, t)
                                            }
                                    }
                                }
                                acc
                            }
                        )

                        parent
                    }
                    type.isEnum() -> {
                        val test = type.memberScope.getClassifierNames()?.map { it.asString() }?.minus("Companion")?.toMutableList()
                            ?: mutableListOf()

                        val enum =
                            type.memberScope.getContributedDescriptors(DescriptorKindFilter.VALUES).map { it.name.asString() }
                                .filterNot {
                                    listOf(
                                        "name",
                                        "ordinal",
                                        "clone",
                                        "compareTo",
                                        "equals",
                                        "finalize",
                                        "getDeclaringClass",
                                        "hashCode",
                                        "toString"
                                    ).contains(it)
                                }

                        // if (enum.isEmpty()) {
                        //                enum.add(type.memberScope.getContributedDescriptors(DescriptorKindFilter.VALUES).map { it.name }
                        //                    .toString())
                        // }
                        parent.properties?.put(
                            propertyName, ObjectType(
                                "string", enum =
                                enum.plus(test)
                            )
                        )
                        parent
                    }
                    type.isMap() -> {
                        val types = type.arguments.map {
                            DescriptorUtils.getClassDescriptorForType(it.type)
                        }

                        types.forEach { d ->
                            val o = ObjectType("object", mutableMapOf()).let { acc: ObjectType ->
                                val classType = d.defaultType
                                var t = acc
                                while (t.items != null) {
                                    t = t.items!!
                                }
                                when {
                                    KotlinBuiltIns.isPrimitiveType(classType) || KotlinBuiltIns.isString(classType) -> {
                                        t.type = d.name.asString().toSwaggerType()
                                        t.properties = null
                                    }
                                    classType.isIterable() || classType.isArrayOrNullableArray() -> {
                                        t.type = "array"
                                        //parent.properties = null
                                        t.items = ObjectType("object", mutableMapOf())
                                    }
                                    classType.isEnum() -> {
                                        t.type = "string"
                                        t.enum = classType.memberScope.getClassifierNames()?.map { it.asString() }?.minus("Companion")
                                    }
                                    KotlinBuiltIns.isAny(classType) -> {
                                        t.type = "object"
                                    }
                                    else -> {
                                        t.type = "object"

                                        if (!classNames.names.contains(name)) {
                                            t.name = name
                                            classNames.add(t)

                                            classType.memberScope
                                                .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                                                .forEach { nestedDescr: DeclarationDescriptor ->
                                                    nestedDescr.accept(this, t)
                                                }
                                        }
                                    }
                                }
                                acc
                            }
                            parent.properties?.put(propertyName, o)
                        }
                        parent
                    }
                    type.isAny() ->{
                        parent
                    }
                    else -> {

                        val kotlinJsonName = descriptor.annotations
                            .firstNotNullOfOrNull { it.allValueArguments[Name.identifier("name")]?.value }
                            ?.toString()

                        if (!classNames.names.contains(name)) {

                            val internal = ObjectType("object",
                                mutableMapOf(),
                                name = name
                            )
                            classNames.add(internal)
                            parent.properties?.put(
                                descriptor.name.asString(),
                                internal
                            )
                            type.memberScope
                                .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                                .forEach { nestedDescr ->
                                    internal.properties?.put(
                                        nestedDescr.name.asString(),
                                        nestedDescr.accept(this, ObjectType("object", mutableMapOf()))
                                    )
                                }
                        }
                        parent
                    }
                }
            }
        }
        return h
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
    return when (this.lowercase()) {
        "int" -> "integer"
        "double" -> "number"
        else -> this.lowercase()
    }
}
