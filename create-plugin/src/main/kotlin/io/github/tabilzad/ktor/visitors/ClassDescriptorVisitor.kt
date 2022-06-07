package io.github.tabilzad.ktor.visitors

import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
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
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.supertypes

class ClassDescriptorVisitor(val context: BindingContext) :
    DeclarationDescriptorVisitorEmptyBodies<ObjectType, ObjectType>() {
    private val classNames = mutableListOf<String>()

    override fun visitClassDescriptor(descriptor: ClassDescriptor?, parent: ObjectType): ObjectType {
        val type = descriptor?.defaultType!!
        return if (KotlinBuiltIns.isPrimitiveType(type) || KotlinBuiltIns.isString(type)) {
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
            type.memberScope
                .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                .forEach { nestedDescr: DeclarationDescriptor ->
                    nestedDescr.accept(this, parent)
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

        val h = if (KotlinBuiltIns.isPrimitiveType(type) || KotlinBuiltIns.isString(type)) {
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
        } else if (type.isIterable() || type.isArrayOrNullableArray()) {

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
                        val jetTypeFqName = type.getJetTypeFqName(true)
                        if (!classNames.contains(jetTypeFqName)) {
                            classNames.add(jetTypeFqName)

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
        } else if (type.isEnum()) {
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
        } else if (type.isMap()) {
            val types = type.arguments.map {
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
                        val jetTypeFqName = type.getJetTypeFqName(true)
                        if (!classNames.contains(jetTypeFqName)) {
                            classNames.add(jetTypeFqName)

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
        } else {

            val kotlinJsonName = descriptor.annotations
                .firstNotNullOfOrNull { it.allValueArguments[Name.identifier("name")]?.value }
                ?.toString()

            val internal = ObjectType("object", mutableMapOf())
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
            parent
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
    return getJetTypeFqName(false)==(DefaultBuiltIns.Instance.map.defaultType.getJetTypeFqName(false))
}

fun KotlinType.unfoldNestedParameters(params: List<TypeProjection> = this.arguments): List<TypeProjection> {

    return arguments.flatMap {
        it.type.unfoldNestedParameters()
    }.plus(asTypeProjection())

//    return if (params.isNotEmpty()) {
//        params.first().type.unfoldNestedParameters()
//    } else {
//        arguments
//    }
}

private fun String.toSwaggerType(): String {
    return when (this.lowercase()) {
        "int" -> "integer"
        "double" -> "number"
        else -> this.lowercase()
    }
}
