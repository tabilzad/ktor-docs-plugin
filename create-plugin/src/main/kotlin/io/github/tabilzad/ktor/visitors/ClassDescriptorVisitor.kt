package io.github.tabilzad.ktor.visitors

import arrow.meta.phases.resolve.unwrappedNotNullableType
import io.github.tabilzad.ktor.KtorDescription
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import io.github.tabilzad.ktor.PluginConfiguration
import io.github.tabilzad.ktor.forEachVariable
import io.github.tabilzad.ktor.names
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.isAny
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.util.removeSuffixIfPresent

class ClassDescriptorVisitor(val config: PluginConfiguration, val context: BindingContext) :
    DeclarationDescriptorVisitorEmptyBodies<ObjectType, ObjectType>() {

    val classNames = mutableListOf<ObjectType>()

    override fun visitPropertyDescriptor(
        descriptor: PropertyDescriptor,
        parent: ObjectType
    ): ObjectType {

        ForceResolveUtil.forceResolveAllContents(descriptor.annotations)
        val type = descriptor.type
        val propertyName = descriptor.resolvePropertyName()
        val docsDescription = descriptor.findDocsDescription()

        val result = when {
            KotlinBuiltIns.isPrimitiveType(type.unwrappedNotNullableType) || KotlinBuiltIns.isString(type.unwrappedNotNullableType) -> {
                if (parent.type == "object") {
                    parent.properties?.put(
                        propertyName,
                        ObjectType(
                            type.toString().toSwaggerType(),
                            description = docsDescription
                        )
                    )
                }
                if (parent.type == "array") {

                    val thisPrimitiveObj = ObjectType(
                        type.toString().toSwaggerType(),
                        description = docsDescription
                    )
                    parent.items = thisPrimitiveObj
                }
                parent
            }

            else -> {
                val fqClassName = type.getKotlinTypeFqName(true)

                when {
                    type.isIterable() || type.isArrayOrNullableArray() -> {

                        parent.properties?.put(
                            propertyName,
                            listObjectType(type, docsDescription)
                        )
                        parent
                    }

                    type.isEnum() -> {

                        val enumValues = type.memberScope.resolveEnumValues()

                        parent.properties?.put(
                            propertyName, ObjectType(
                                "string",
                                enum = enumValues,
                                description = docsDescription
                            )
                        )
                        parent
                    }

                    type.isMap() -> {

                        val valueType = type.arguments.last()

                        fun TypeProjection.createMapDefinition(): ObjectType {
                            val classDescriptor = DescriptorUtils.getClassDescriptorForType(this.type)
                            val valueClassType = classDescriptor.defaultType
                            val acc = ObjectType("object", mutableMapOf())


                            when {
                                KotlinBuiltIns.isPrimitiveType(valueClassType) || KotlinBuiltIns.isString(
                                    valueClassType
                                ) -> {
                                    acc.additionalProperties =
                                        ObjectType(classDescriptor.name.asString().toSwaggerType())
                                }

                                valueClassType.isIterable() || valueClassType.isArrayOrNullableArray() -> {
                                    acc.type = "object"
                                    acc.additionalProperties = listObjectType(
                                        type = this@createMapDefinition.type,
                                        docsDescription = docsDescription,
                                    )
                                }

                                valueClassType.isEnum() -> {
                                    acc.type = "object"
                                    acc.additionalProperties = ObjectType("string",
                                        enum = valueClassType.memberScope.getClassifierNames()?.map { it.asString() }
                                            ?.minus("Companion")
                                    )
                                }

                                KotlinBuiltIns.isAny(valueClassType) -> {
                                    acc.type = "object"
                                }

                                else -> {
                                    val gName = valueClassType.getKotlinTypeFqName(false)
                                    if (!classNames.names.contains(gName)) {
                                        val q = ObjectType(
                                            "object",
                                            mutableMapOf(),
                                            fqName = gName,
                                            description = docsDescription
                                        )
                                        classNames.add(q)

                                        valueClassType.memberScope
                                            .forEachVariable(config) { nestedDescr: DeclarationDescriptor ->
                                                nestedDescr.accept(this@ClassDescriptorVisitor, q)
                                            }
                                    }

                                    acc.additionalProperties = ObjectType(
                                        type = null,
                                        ref = "#/components/schemas/${gName}"
                                    )
                                }
                            }

                            return acc
                        }

                        val item = valueType.createMapDefinition()
                        parent.properties?.put(
                            propertyName,
                            item
                        )
                        parent
                    }

                    type.isAny() -> {
                        parent
                    }

                    else -> {

                        if (!classNames.names.contains(fqClassName)) {
                            val internal = ObjectType(
                                "object",
                                mutableMapOf(),
                                fqName = fqClassName
                            )
                            classNames.add(internal)
                            type.memberScope
                                .forEachVariable(config) { nestedDescr ->
                                    nestedDescr.accept(this, internal)
                                }
                        }
                        if (parent.properties != null) {
                            parent.properties?.put(
                                propertyName,
                                ObjectType(
                                    type = null,
                                    fqName = fqClassName,
                                    description = docsDescription,
                                    ref = "#/components/schemas/${fqClassName}"
                                )
                            )
                        } else {
                            parent.properties = mutableMapOf(
                                propertyName to
                                        ObjectType(
                                            type = null,
                                            fqName = fqClassName,
                                            description = docsDescription,
                                            ref = "#/components/schemas/${fqClassName}"
                                        )
                            )
                        }
                        parent
                    }
                }
            }
        }
        return result
    }

    private fun listObjectType(
        type: KotlinType,
        docsDescription: String?
    ): ObjectType {
        val types = type.unfoldNestedParameters().reversed().map {
            DescriptorUtils.getClassDescriptorForType(it.type)
        }

        return types.fold(
            ObjectType(
                "object",
                mutableMapOf(),
                description = docsDescription
            )
        ) { acc: ObjectType, d: ClassDescriptor ->
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
                t.items = ObjectType(null, mutableMapOf())
            } else if (classType.isEnum()) {
                t.type = "string"
                t.enum = classType.memberScope.resolveEnumValues()
            } else {
                t.type = null
                val jetTypeFqName = classType.getKotlinTypeFqName(false)

                if (!classNames.names.contains(jetTypeFqName)) {

                    val q = ObjectType(
                        "object",
                        properties = mutableMapOf(),
                        fqName = jetTypeFqName,
                        description = docsDescription
                    )
                    classNames.add(q)
                    classType.memberScope
                        .forEachVariable(config) { nestedDescr: DeclarationDescriptor ->
                            nestedDescr.accept(this, q)
                        }
                }
                t.apply {
                    ref = "#/components/schemas/${jetTypeFqName}"
                    properties = null
                }

            }
            acc
        }
    }


    private fun PropertyDescriptor.resolvePropertyName(): String {

        val moshiJson = FqName("com.squareup.moshi.Json")
        return if (backingField?.annotations?.hasAnnotation(moshiJson) == true) {

            val an = backingField?.annotations?.findAnnotation(moshiJson)

            val value = an?.allValueArguments?.get(Name.identifier("name"))

            value?.value?.toString() ?: name.toString()
        } else {
            name.toString()
        }
    }
}

private fun PropertyDescriptor.findDocsDescription(): String? {
    val text =
        backingField
            ?.annotations
            ?.find { it.fqName?.asString()?.contains(KtorDescription::class.simpleName!!) == true }
            ?.let { annotationDescriptor ->
                val summary = annotationDescriptor.allValueArguments.get(Name.identifier("summary"))?.value as? String
                val description =
                    annotationDescriptor.allValueArguments.get(Name.identifier("description"))?.value as? String
                summary ?: description
            }
    return text
}

fun KotlinType.isIterable(): Boolean {
    return supertypes().map { it.getJetTypeFqName(false) }
        .contains(DefaultBuiltIns.Instance.iterableType.getJetTypeFqName(false))
}

fun KotlinType.isMap(): Boolean {
    val jetTypeFqName = getKotlinTypeFqName(false)
    return listOf(
        DefaultBuiltIns.Instance.map.defaultType.getKotlinTypeFqName(false),
        DefaultBuiltIns.Instance.mutableMap.defaultType.getKotlinTypeFqName(false)
    ).any { it == jetTypeFqName }
}

fun KotlinType.unfoldNestedParameters(params: List<TypeProjection> = this.arguments): List<TypeProjection> {
    return arguments.flatMap {
        it.type.unfoldNestedParameters()
    }.plus(asTypeProjection())
}

fun MemberScope.resolveEnumValues(): List<String> {
    return getContributedDescriptors(DescriptorKindFilter.VALUES)
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
