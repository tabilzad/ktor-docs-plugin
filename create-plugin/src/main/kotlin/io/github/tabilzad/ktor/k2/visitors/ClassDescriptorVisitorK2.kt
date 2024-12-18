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
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProviderInternals
import org.jetbrains.kotlin.fir.declarations.sealedInheritorsAttr
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

    override fun visitProperty(property: FirProperty, data: ObjectType): ObjectType {
        val coneTypeOrNull = property.returnTypeRef.coneTypeOrNull!!
        val type = if (coneTypeOrNull is ConeTypeParameterType && genericParameters.isNotEmpty()) {
            genericParameters.find { it.genericName == coneTypeOrNull.renderReadable() }?.genericTypeRef!!
        } else {
            coneTypeOrNull
        }

        return data.apply { addProperty(property, type.collectDataTypes(), session) }
    }


    @OptIn(SealedClassInheritorsProviderInternals::class, SymbolInternals::class)
    private fun ConeKotlinType.collectDataTypes(): ObjectType? {
        val fqClassName = fqNameStr()
        val typeSymbol = toRegularClassSymbol(session)

        return when {
            isPrimitive || isPrimitiveOrNullablePrimitive || type.isString || type.isNullableString -> {
                ObjectType(type = type.className()?.toSwaggerType() ?: "Unknown")
            }

            isMap() -> {
                val valueType = typeArguments.last()

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
                                acc.additionalProperties = valueClassType.collectDataTypes()
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

                item
            }

            isIterable() -> {
                val arrayItemType = typeArguments.firstNotNullOfOrNull { it.type }
                // list only take a single generic type
                val array = ObjectType("array", items = arrayItemType?.collectDataTypes())
                array
            }

            isEnum || typeSymbol?.isEnumClass == true -> {

                val enumValues = typeSymbol?.resolveEnumEntries()


                ObjectType(
                    type = "string",
                    enum = enumValues,
                    //description = docsDescription
                )
            }

            typeSymbol?.isSealed == true -> {

                if (!classNames.names.contains(fqClassName)) {
                    val inheritorClassIds = typeSymbol.fir.sealedInheritorsAttr?.getValueOrNull()
                    val internal = ObjectType("object",
                        fqName = fqClassName,
                        oneOf = inheritorClassIds?.map { OpenApiSpec.SchemaRef("#/components/schemas/${it.asFqNameString()}") })
                    classNames.add(internal)
                    getMembers(session, config).forEach { nestedDescr ->
                        nestedDescr.accept(this@ClassDescriptorVisitorK2, internal)
                    }

                    inheritorClassIds?.forEach { it: ClassId ->

                        val inheritorType = ObjectType(
                            "object",
                            fqName = it.asFqNameString(),
                        )
                        classNames.add(inheritorType)
                        val fir: FirClass? = it.toLookupTag().toClassSymbol(session)?.fir
                        fir?.accept(this@ClassDescriptorVisitorK2, inheritorType)
                    }
                }

                ObjectType(
                    type = null,
                    fqName = fqClassName,
                    //description = docsDescription,
                    ref = "#/components/schemas/${fqClassName}"
                )
            }

            isAny -> {
                null
            }

            isValueClass(session) -> {
                ObjectType(
                    properties(session)?.firstOrNull()?.resolvedReturnType?.className()
                        ?.toSwaggerType(),
                    fqName = fqClassName
                )
            }

            else -> {

                if (!classNames.names.contains(fqClassName)) {
                    val internal = ObjectType(
                        "object",
                        fqName = fqClassName
                    )
                    classNames.add(internal)

                    if (typeArguments.isEmpty()) {
                        getMembers(session, config).forEach { nestedDescr ->
                            nestedDescr.accept(this@ClassDescriptorVisitorK2, internal)
                        }
                    } else {
                        val things = getMembers(session, config)
                            .map { nestedDescr ->
                                nestedDescr.accept(
                                    ClassDescriptorVisitorK2(
                                        config, session, context,
                                        classNames = classNames,
                                        genericParameters = typeArguments.zip(
                                            typeSymbol?.typeParameterSymbols ?: emptyList()
                                        ).map { (specifiedType, genericType) ->
                                            GenericParameter(
                                                genericTypeRef = specifiedType.type,
                                                genericName = genericType.name.asString()
                                            )
                                        }), internal
                                )
                            }
                    }
                }

                ObjectType(
                    type = null,
                    fqName = fqClassName,
                    //description = docsDescription,
                    ref = "#/components/schemas/${fqClassName}"
                )
            }
        }
    }

    override fun visitClass(klass: FirClass, data: ObjectType): ObjectType {
        klass.defaultType().getMembers(session, config).forEach { it.accept(this, data) }
        return data
    }

    override fun visitElement(element: FirElement, data: ObjectType) = data

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
