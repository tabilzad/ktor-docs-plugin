package io.github.tabilzad.ktor.k2.visitors

import io.github.tabilzad.ktor.PluginConfiguration
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorFieldDescription
import io.github.tabilzad.ktor.extractDescription
import io.github.tabilzad.ktor.getKDocComments
import io.github.tabilzad.ktor.k1.visitors.KtorDescriptionBag
import io.github.tabilzad.ktor.k1.visitors.toSwaggerType
import io.github.tabilzad.ktor.k2.*
import io.github.tabilzad.ktor.k2.ClassIds.KTOR_FIELD_DESCRIPTION
import io.github.tabilzad.ktor.k2.JsonNameResolver.getCustomNameFromAnnotation
import io.github.tabilzad.ktor.names
import io.github.tabilzad.ktor.output.OpenApiSpec
import io.github.tabilzad.ktor.output.OpenApiSpec.ObjectType
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.isValueClass
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isSealed
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.toClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.util.getValueOrNull

data class GenericParameter(
    val genericName: String,
    val genericTypeRef: ConeKotlinType?
)

internal class ClassDescriptorVisitorK2(
    private val config: PluginConfiguration,
    private val session: FirSession,
    private val context: CheckerContext,
    private val genericParameters: List<GenericParameter> = emptyList(),
    val classNames: MutableList<ObjectType> = mutableListOf(),
) : FirDefaultVisitor<ObjectType, ObjectType>() {


    @OptIn(SealedClassInheritorsProviderInternals::class, SymbolInternals::class)
    override fun visitProperty(property: FirProperty, data: ObjectType): ObjectType {
        val coneTypeOrNull = property.returnTypeRef.coneTypeOrNull!!
        val type = if (coneTypeOrNull is ConeTypeParameterType && genericParameters.isNotEmpty()) {
            genericParameters.find { it.genericName == coneTypeOrNull.renderReadable() }?.genericTypeRef!!
        } else {
            coneTypeOrNull
        }
        val typeSymbol = type.toRegularClassSymbol(session)

        val result = when {
            type.isPrimitiveOrNullablePrimitive || type.isString || type.isNullableString -> {
                if (data.type == "object") {
                    data.addProperty(
                        property,
                        objectType = ObjectType(
                            type = type.className()?.toSwaggerType() ?: "Unknown",
                        ), session
                    )
                }
                if (data.type == "array") {

                    val thisPrimitiveObj = ObjectType(
                        type.toString().toSwaggerType(),
                    )
                    data.items = thisPrimitiveObj
                }
                data
            }

            else -> {
                val fqClassName = type.fqNameStr()

                when {
                    type.isMap() -> {

                        val valueType = type.typeArguments.last()

                        fun ConeTypeProjection.createMapDefinition(): ObjectType? {
                            return this.type?.let { valueClassType ->
                                val typeSymbol = valueClassType.toRegularClassSymbol(session)
                                val acc = ObjectType("object", mutableMapOf())

                                when {
                                    valueClassType.isPrimitiveOrNullablePrimitive || valueClassType.isString || valueClassType.isNullableString -> {
                                        acc.additionalProperties =
                                            ObjectType(valueClassType.className()?.toSwaggerType())
                                    }

                                    valueClassType.isIterable() -> {
                                        acc.type = "object"
                                        acc.additionalProperties = valueClassType.toNestedSwagger()
                                    }

                                    valueClassType.isEnum || typeSymbol?.isEnumClass == true -> {
                                        acc.type = "object"
                                        acc.additionalProperties = ObjectType(
                                            "string", enum = typeSymbol?.resolveEnumEntries()
                                        )
                                    }

                                    valueClassType.isAny -> {
                                        acc.type = "object"
                                    }

                                    else -> {
                                        val gName = valueClassType.fqNameStr()
                                        if (!classNames.names.contains(gName)) {
                                            val q = ObjectType(
                                                "object",
                                                null,
                                                fqName = gName,
                                                //description = docsDescription
                                            )
                                            classNames.add(q)

                                            valueClassType.getMembers(session, config).forEach { it ->
                                                it.accept(this@ClassDescriptorVisitorK2, q)
                                            }
                                        }

                                        acc.additionalProperties = ObjectType(
                                            type = null, ref = "#/components/schemas/${gName}"
                                        )
                                    }
                                }

                                acc
                            }
                        }

                        val item = valueType.createMapDefinition()
                        data.addProperty(property, item, session)
                        data
                    }

                    type.isIterable() -> {
                        data.addProperty(property, type.toNestedSwagger(), session)
                        data
                    }

                    type.isEnum || typeSymbol?.isEnumClass == true -> {

                        val enumValues = typeSymbol?.resolveEnumEntries()
                        data.addProperty(
                            property, ObjectType(
                                type = "string",
                                enum = enumValues,
                                //description = docsDescription
                            ),
                            session
                        )
                        data
                    }

                    typeSymbol?.isSealed == true -> {

                        if (!classNames.names.contains(fqClassName)) {
                            val inheritorClassIds = typeSymbol.fir.sealedInheritorsAttr?.getValueOrNull()
                            val internal = ObjectType("object",
                                fqName = fqClassName,
                                oneOf = inheritorClassIds?.map { OpenApiSpec.SchemaRef("#/components/schemas/${it.asFqNameString()}") })
                            classNames.add(internal)
                            type.getMembers(session, config).forEach { nestedDescr ->
                                nestedDescr.accept(this, internal)
                            }

                            inheritorClassIds?.forEach { it: ClassId ->

                                val inheritorType = ObjectType(
                                    "object",
                                    fqName = it.asFqNameString(),
                                )
                                classNames.add(inheritorType)
                                val fir: FirClass? = it.toLookupTag().toClassSymbol(session)?.fir
                                fir?.accept(this, inheritorType)
                            }
                        }

                        data.addProperty(
                            property, ObjectType(
                                type = null,
                                fqName = fqClassName,
                                //description = docsDescription,
                                ref = "#/components/schemas/${fqClassName}"
                            ), session
                        )

                        data
                    }

                    type.isAny -> {
                        data
                    }

                    type.isValueClass(session) -> {
                        data.addProperty(
                            property, ObjectType(
                                type.properties(session)?.firstOrNull()?.resolvedReturnType?.className()
                                    ?.toSwaggerType(),
                                fqName = fqClassName
                            ), session
                        )
                        data
                    }

                    else -> {

                        if (!classNames.names.contains(fqClassName)) {
                            val internal = ObjectType(
                                "object",
                                fqName = fqClassName
                            )
                            classNames.add(internal)

                            if (type.typeArguments.isEmpty()) {
                                type.getMembers(session, config).forEach { nestedDescr ->
                                    nestedDescr.accept(this, internal)
                                }
                            } else {
                                val things = type.getMembers(session, config)
                                    .map { nestedDescr ->
                                        nestedDescr.accept(
                                            ClassDescriptorVisitorK2(config, session, context,
                                                classNames = classNames,
                                                genericParameters = type.typeArguments.zip(
                                                    typeSymbol?.typeParameterSymbols ?: emptyList()
                                                ).map { (specifiedType, genericType) ->
                                                    GenericParameter(
                                                        genericTypeRef = specifiedType.type,
                                                        genericName = genericType.name.asString()
                                                    )
                                                }), internal
                                        )
                                    }

                                println()
                            }
                        }

                        data.addProperty(
                            property, ObjectType(
                                type = null,
                                fqName = fqClassName,
                                //description = docsDescription,
                                ref = "#/components/schemas/${fqClassName}"
                            ), session
                        )

                        data
                    }
                }
            }
        }


        return result
    }

    override fun visitClass(klass: FirClass, data: ObjectType): ObjectType {
        klass.defaultType().getMembers(session, config).forEach { it.accept(this, data) }
        return data
    }

    override fun visitElement(element: FirElement, data: ObjectType): ObjectType {
        return data
    }

    private fun ConeKotlinType.toNestedSwagger(): ObjectType {
        val arrayItems = type.typeArguments.mapNotNull { it.type }.map {
            convertSubTree(it)
        }
        return ObjectType("array", items = arrayItems.firstOrNull())// list only take a single generic type
    }

    private fun convertSubTree(type: ConeKotlinType?): ObjectType? {
        type?.typeArguments?.mapNotNull { it.type }?.map { convertSubTree(it) }
        return type?.resolveItems()
    }


    private fun ConeKotlinType.resolveItems(
    ): ObjectType? {
        val type = this
        val typeSymbol = toRegularClassSymbol(session)
        val jetTypeFqName = fqNameStr()
        return if (type.isPrimitive || type.isString || type.isPrimitiveOrNullablePrimitive || isNullableString) {
            ObjectType(type.className()?.toSwaggerType())
        } else if (type.isIterable()) {
            ObjectType("array", items = this.typeArguments.firstNotNullOfOrNull { it.type }?.resolveItems())
        } else if (type.isEnum || typeSymbol?.isEnumClass == true) {
            ObjectType("string").apply {
                enum = typeSymbol?.resolveEnumEntries()
            }
        } else {
            if (!classNames.names.contains(jetTypeFqName)) {

                val refObject = ObjectType(
                    "object", properties = null, fqName = jetTypeFqName
                )
                classNames.add(refObject)
                type.getMembers(session, config).forEach { it: FirDeclaration ->
                    // it.accept(this@ClassDescriptorVisitorK2, q)
                    it.accept(
                        ClassDescriptorVisitorK2(config, session, context,
                            classNames = classNames,
                            genericParameters = type.typeArguments.zip(
                                typeSymbol?.typeParameterSymbols ?: emptyList()
                            ).map { (specifiedType, genericType) ->
                                GenericParameter(
                                    genericTypeRef = specifiedType.type,
                                    genericName = genericType.name.asString()
                                )
                            }), refObject
                    )
                }
            }
            ObjectType(null).apply {
                ref = "#/components/schemas/${jetTypeFqName}"
            }
        }
    }

    private fun ObjectType.addProperty(fir: FirProperty, objectType: ObjectType?, session: FirSession) {
        val kdoc = fir.getKDocComments(config)
        val resolvedDescription = fir.findDocsDescription(session)
        val docsDescription = resolvedDescription.let { it?.summary ?: it?.description }
        val name = fir.findName()
        val spec = objectType ?: ObjectType("object")
        if (properties == null) {
            properties = mutableMapOf(name to spec)
        } else {
            properties?.put(name, spec)
        }

        objectType?.description = docsDescription ?: kdoc

        val isRequiredFromExplicitDesc = resolvedDescription?.isRequired
        if (isRequiredFromExplicitDesc != null && isRequiredFromExplicitDesc) {
            required?.add(name) ?: run {
                required = mutableListOf(name)
            }
        } else if ((isRequiredFromExplicitDesc == null && fir.returnTypeRef.isMarkedNullable == false)
            && config.deriveFieldRequirementFromTypeNullability
        ) {
            required?.add(name) ?: run {
                required = mutableListOf(name)
            }
        }
    }

    private fun FirProperty.findName(): String {
        return getCustomNameFromAnnotation(this, session) ?: name.asString()
    }

}

internal fun FirProperty.findDocsDescription(session: FirSession): KtorDescriptionBag? {
    val docsAnnotation =
        findAnnotation(KtorDescription::class.simpleName) ?: findAnnotation(KtorFieldDescription::class.simpleName)
        ?: return null

    val dataBag = docsAnnotation.extractDescription(session)
    return dataBag.copy(
        isRequired = dataBag.isRequired ?: (returnTypeRef.isMarkedNullable == false)
    )
}

internal fun ConeKotlinType.findDocsDescription(session: FirSession): KtorDescriptionBag? {
    val docsAnnotation = toRegularClassSymbol(session)?.annotations
        ?.find { it.fqName(session) == KTOR_FIELD_DESCRIPTION }

    if (docsAnnotation == null) return null
    val dataBag = docsAnnotation.extractDescription(session)
    return dataBag.copy(
        isRequired = dataBag.isRequired ?: (!isNullable)
    )
}
