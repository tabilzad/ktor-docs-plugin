package io.github.tabilzad.ktor.k2

import io.github.tabilzad.ktor.*
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.k1.visitors.KtorDescriptionBag
import io.github.tabilzad.ktor.k1.visitors.toSwaggerType
import io.github.tabilzad.ktor.k2.visitors.*
import io.github.tabilzad.ktor.output.OpenApiSpec
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.isValueClass
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.resolved
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.firClassLike
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.toFirRegularClass
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.util.PrivateForInline

internal class ExpressionsVisitorK2(
    private val config: PluginConfiguration,
    private val context: CheckerContext,
    private val session: FirSession,
    private val log: MessageCollector?
) : FirDefaultVisitor<List<KtorElement>, KtorElement?>() {

    init {
        println("BeginK2 Visitor")
    }

    val classNames = mutableListOf<OpenApiSpec.ObjectType>()

    override fun visitElement(expression: FirElement, parent: KtorElement?): List<KtorElement> {
        return parent.wrapAsList()
    }

    @OptIn(PrivateForInline::class)
    private fun FirStatement.findTags(session: FirSession): Set<String>? {
        val annotation = findAnnotation(ClassIds.KTOR_TAGS_ANNOTATION, session) ?: return null
        val resolved = FirExpressionEvaluator.evaluateAnnotationArguments(annotation, session)
        return resolved?.entries?.find { it.key.asString() == "tags" }?.value?.result?.accept(
            StringArrayLiteralVisitor(),
            emptyList()
        )?.toSet()
    }

    // Evaluation Order 1
    override fun visitSimpleFunction(simpleFunction: FirSimpleFunction, parent: KtorElement?): List<KtorElement> {

        val extractedTags = simpleFunction.findTags(session)
        val c = parent ?: DocRoute("/", tags = extractedTags)

        simpleFunction.acceptChildren(this, c)
        return c.wrapAsList()
    }


    // Evaluation Order 2
    override fun visitDeclaration(declaration: FirDeclaration, parent: KtorElement?): List<KtorElement> {
        return if (declaration is FirSimpleFunction) {
            declaration.body?.accept(this, parent) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Evaluation Order 3
    override fun visitBlock(block: FirBlock, parent: KtorElement?): List<KtorElement> {

        if (parent is EndPoint && parent.body == null) {

            val receiveCall = block.statements.findReceiveCallExpression()

            val queryParam = block.statements.findQueryParameterExpression()
            if (queryParam.isNotEmpty()) parent.parameters = parent.parameters merge queryParam.toSet()

            val headerParam = block.statements.findHeaderParameterExpression()
            if (headerParam.isNotEmpty()) parent.parameters = parent.parameters merge headerParam.toSet()

            if (receiveCall != null) {
                val kotlinType = receiveCall.resolvedType
                if (kotlinType.isPrimitiveOrNullablePrimitive || kotlinType.isString || kotlinType.isNullableString) {
                    parent.body = OpenApiSpec.ObjectType(type = kotlinType.toString().toSwaggerType())
                } else {
                    if (config.requestBody) {
                        parent.body = kotlinType.generateTypeAndVisitMemberDescriptors()
                    }
                }
            }
        }

        return block.statements.flatMap { it.accept(this, parent) }
    }


    @OptIn(SymbolInternals::class)
    private fun ConeKotlinType.generateTypeAndVisitMemberDescriptors(): OpenApiSpec.ObjectType {

        val jetTypeFqName = fqNameStr()

        val kdocs = toRegularClassSymbol(session)
            ?.toLookupTag()
            ?.toFirRegularClass(session)
            ?.getKDocComments(config)

        val annotatedDescription = findDocsDescription(session)

        val objectType = OpenApiSpec.ObjectType(
            type = "object",
            properties = mutableMapOf(),
            fqName = jetTypeFqName,
            description = kdocs ?: annotatedDescription?.description ?: annotatedDescription?.summary,
            contentBodyRef = "#/components/schemas/${jetTypeFqName}",
        )

        if (isValueClass(session)) {
            return objectType.copy(
                type = properties(session)?.firstOrNull()
                    ?.resolvedReturnType
                    ?.className()
                    ?.toSwaggerType(),
                properties = null
            ).also {
                if (!classNames.names.contains(jetTypeFqName)) {
                    classNames.add(it)
                }
            }
        } else {

            if (!classNames.names.contains(jetTypeFqName)) {

                classNames.add(objectType)

                getMembers(session, config).forEach { d ->

                    val classDescriptorVisitor = ClassDescriptorVisitorK2(config, session, context)
                    d.accept(classDescriptorVisitor, objectType)
                    classNames.addAll(classDescriptorVisitor.classNames)
                }
            }
            return objectType
        }
    }

    private fun List<FirStatement>.findQueryParameterExpression(): List<ParamSpec> {
        val queryParams = mutableListOf<String>()
        flatMap { it.allChildren }.filterIsInstance<FirFunctionCall>()
            .forEach {
                it.accept(
                    ParametersVisitor(
                        session,
                        listOf(ClassIds.KTOR_QUERY_PARAM, ClassIds.KTOR_RAW_QUERY_PARAM)
                    ), queryParams
                )
            }
        return queryParams.map { QueryParamSpec(it) }
    }

    private fun List<FirStatement>.findHeaderParameterExpression(): List<ParamSpec> {
        val headerParams = mutableListOf<String>()
        flatMap { it.allChildren }.filterIsInstance<FirFunctionCall>()
            .forEach {
                it.accept(
                    ParametersVisitor(
                        session,
                        listOf(ClassIds.KTOR_HEADER_PARAM, ClassIds.KTOR_HEADER_ACCESSOR)
                    ), headerParams
                )
            }

        filterIsInstance<FirFunctionCall>()
            .forEach {
                it.accept(
                    ParametersVisitor(session, listOf(ClassIds.KTOR_HEADER_ACCESSOR)),
                    headerParams
                )
            }

        return headerParams.map { HeaderParamSpec(it) }
    }

    private fun List<FirStatement>.findReceiveCallExpression(): FirFunctionCall? {

        val receiveFunctionCall = filterIsInstance<FirFunctionCall>()
            .find { it.toResolvedCallableSymbol()?.callableId?.asSingleFqName() == ClassIds.KTOR_RECEIVE }

        if (receiveFunctionCall == null) {
            return flatMap { it.allChildren }.filterIsInstance<FirFunctionCall>()
                .find { it.toResolvedCallableSymbol()?.callableId?.asSingleFqName() == ClassIds.KTOR_RECEIVE }
        }
        return receiveFunctionCall
    }


    private fun FirElement?.isKtorApplicationCall(): Boolean {
        return if (this is FirQualifiedAccessExpression) {
            extensionReceiver?.resolvedType?.classId == ClassIds.KTOR_APPLICATION
        } else {
            false
        }
    }

    private fun FirSimpleFunction.isKtorApplicationCall(): Boolean {
        val classId = receiverParameter?.typeRef?.firClassLike(session)?.classId
        return classId == ClassIds.KTOR_APPLICATION || classId == ClassIds.KTOR_ROUTE
    }

    private fun FirQualifiedAccessExpression?.isARouteDefinition(): Boolean {
        return this?.resolvedType?.classId == ClassIds.KTOR_ROUTE
    }


    @OptIn(SymbolInternals::class)
    private fun FirFunctionCall.resolvePath(): String? {
        val expression = this
        val pathExpressionIndex = (expression.calleeReference.resolved?.resolvedSymbol?.fir as FirFunction)
            .valueParameters.indexOfFirst { it.name.asString() == "path" }

        val pathExpression = expression.arguments.getOrElse(pathExpressionIndex) {
            expression.arguments.find { it is FirLiteralExpression }
        }
        val resolvedPathValue = pathExpression?.accept(StringResolutionVisitor(session), "")
        return resolvedPathValue
    }

    private fun FirFunctionCall.findLambda(): FirAnonymousFunctionExpression? {
        return arguments
            .filterIsInstance<FirAnonymousFunctionExpression>()
            .lastOrNull()
    }


    @OptIn(SymbolInternals::class)
    override fun visitFunctionCall(functionCall: FirFunctionCall, parent: KtorElement?): List<KtorElement> {
        var resultElement: KtorElement? = null
        val resolvedExp = functionCall.toResolvedCallableReference(session)
        val expName = resolvedExp?.name?.asString() ?: ""

        val tagsFromAnnotation = functionCall.findTags(session)
        if (functionCall.isARouteDefinition() || ExpType.METHOD.labels.contains(expName)) {

            val pathValue = functionCall.resolvePath()

            if (ExpType.ROUTE.labels.contains(expName)) {
                if (parent == null) {
                    resultElement = pathValue?.let {
                        DocRoute(pathValue, tags = tagsFromAnnotation)
                    } ?: run {
                        DocRoute(expName, tags = tagsFromAnnotation)
                    }
                } else {
                    if (parent is DocRoute) {
                        val newElement = DocRoute(
                            pathValue.toString(),
                            tags = parent.tags merge tagsFromAnnotation
                        )

                        resultElement = newElement
                        parent.children.add(newElement)
                    }
                }
            } else if (ExpType.METHOD.labels.contains(expName)) {
                val descr = functionCall.findDocsDescription(session)
                val responds = functionCall.findRespondsAnnotation(session)
                val responses = responds?.associate { response ->

                    val kotlinType = response.type

                    val schema =
                        if (kotlinType?.isPrimitiveOrNullablePrimitive == true || kotlinType?.isString == true || kotlinType?.isNullableString == true) {
                            OpenApiSpec.SchemaType(
                                type = kotlinType.toString().toSwaggerType()
                            )
                        } else {

                            val typeRef = response.type?.generateTypeAndVisitMemberDescriptors()
                            OpenApiSpec.SchemaType(
                                `$ref` = "${typeRef?.contentBodyRef}"
                            )
                        }
                    if (!response.isCollection) {
                        response.status to
                                OpenApiSpec.ResponseDetails(
                                    response.descr ?: "",
                                    mapOf(
                                        ContentType.APPLICATION_JSON to mapOf(
                                            "schema" to schema
                                        )
                                    )
                                )
                    } else {
                        response.status to
                                OpenApiSpec.ResponseDetails(
                                    response.descr ?: "",
                                    mapOf(
                                        ContentType.APPLICATION_JSON to mapOf(
                                            "schema" to OpenApiSpec.SchemaType(
                                                type = "array",
                                                items = OpenApiSpec.SchemaRef(
                                                    type = schema.type,
                                                    `$ref` = schema.`$ref`
                                                )
                                            )
                                        )
                                    )
                                )
                    }
                }

                val params = functionCall.typeArguments

                var body: OpenApiSpec.ObjectType? = null
                var resource: KtorElement? = null

                val endpoint = EndPoint(
                    path = null,
                    method = expName,
                    description = descr.description,
                    summary = descr.summary,
                    operationId = descr.operationId,
                    tags = descr.tags merge tagsFromAnnotation,
                    responses = responses
                )

                val type = params.firstOrNull()?.toConeTypeProjection()?.type

                if (functionCall.isInPackage(ClassIds.KTOR_RESOURCES) && type.isKtorResourceAnnotated()) {

                    resource = type?.toRegularClassSymbol(session)
                        ?.fir
                        ?.accept(
                            ResourceClassVisitor(
                                session, config, endpoint
                            ), null
                        )

                } else if (functionCall.isInPackage(ClassIds.KTOR_ROUTING_PACKAGE)) {
                    body = type?.toEndpointBody()
                }

                resultElement = when (parent) {
                    null -> resource ?: pathValue?.let {
                        endpoint.copy(path = pathValue, body = body)
                    }

                    is DocRoute -> {
                        val element = resource ?: endpoint.copy(path = pathValue, body = body)
                        parent.children.add(element)
                        element
                    }

                    else -> {
                        log?.report(CompilerMessageSeverity.WARNING, "Endpoints can't have Endpoint as routes", null)
                        null
                    }
                }

            }
        }

        val lambda = functionCall.findLambda()
        if (lambda != null) {
            lambda.accept(this, resultElement ?: parent)
        } else {

            val declaration = functionCall.calleeReference.toResolvedFunctionSymbol()?.fir

            val tagsFromDeclaration = declaration?.findTags(session)

            if (parent is DocRoute) {
                val accept = declaration?.accept(this, null)

                accept?.onEach {
                    it.tags = it.tags merge tagsFromAnnotation merge tagsFromDeclaration
                }
                parent.children.addAll(accept ?: emptyList())
            } else {
                declaration?.accept(this, parent)
            }
        }

        return (resultElement ?: parent).wrapAsList()
    }

    override fun visitReturnExpression(returnExpression: FirReturnExpression, data: KtorElement?): List<KtorElement> {
        val funCall = returnExpression.result as? FirFunctionCall
        funCall?.accept(this, data)
        return super.visitReturnExpression(returnExpression, data)
    }

    override fun visitAnonymousFunctionExpression(
        anonymousFunctionExpression: FirAnonymousFunctionExpression,
        data: KtorElement?
    ): List<KtorElement> = anonymousFunctionExpression.anonymousFunction.body?.accept(this, data) ?: data.wrapAsList()

    override fun visitAnonymousFunction(
        anonymousFunction: FirAnonymousFunction,
        parent: KtorElement?
    ): List<KtorElement> = anonymousFunction.body?.accept(this, parent) ?: parent.wrapAsList()


    private fun ConeKotlinType.toEndpointBody(): OpenApiSpec.ObjectType? {
        return if (isPrimitiveOrNullablePrimitive || isString || isNullableString) {
            OpenApiSpec.ObjectType(type = toString().toSwaggerType())
        } else {
            if (config.requestBody) {
                generateTypeAndVisitMemberDescriptors()
            } else {
                null
            }
        }
    }

    private fun ConeKotlinType?.isKtorResourceAnnotated(): Boolean =
        this?.toRegularClassSymbol(session)?.hasAnnotation(ClassIds.KTOR_RESOURCE_ANNOTATION, session) == true

    private fun FirRegularClassSymbol.findAnnotation(classId: ClassId): FirAnnotation? {
        return annotations.find { it.fqName(session) == classId.asSingleFqName() }
    }

    private fun FirFunctionCall.isInPackage(fqName: FqName): Boolean =
        toResolvedCallableSymbol()?.callableId?.packageName == fqName

}


internal data class KtorK2ResponseBag(
    val descr: String,
    val status: String,
    val type: ConeKotlinType?,
    val isCollection: Boolean = false
)

private fun KtorElement?.wrapAsList() = this?.let { listOf(this) } ?: emptyList()

private fun FirFunctionCall.findDocsDescription(session: FirSession): KtorDescriptionBag {
    val docsAnnotation = findAnnotation(KtorDescription::class.simpleName!!)
        ?: return KtorDescriptionBag()

    return docsAnnotation.extractDescription(session)
}

@OptIn(PrivateForInline::class)
private fun FirFunctionCall.findRespondsAnnotation(session: FirSession): List<KtorK2ResponseBag>? {
    val annotation = findAnnotation(KtorResponds::class.simpleName!!)
    return annotation?.let {
        val resolved = FirExpressionEvaluator.evaluateAnnotationArguments(annotation, session)
        val mapping = resolved?.entries?.find { it.key.asString() == "mapping" }?.value?.result
        mapping?.accept(RespondsAnnotationVisitor(session), null)
    }
}
