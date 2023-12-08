package io.github.tabilzad.ktor.visitors

import ClassDescriptorVisitor
import io.github.tabilzad.ktor.*
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.argumentIndex
import org.jetbrains.kotlin.psi.psiUtil.getAnnotationEntries
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import toSwaggerType

class ExpressionVisitor(
    private val requestBodyFeature: Boolean,
    val context: BindingContext
) : KtVisitor<List<KtorElement>, KtorElement?>() {
    init {
        println("BeginVisitor")
    }

    val classNames = mutableListOf<ObjectType>()
    override fun visitProperty(property: KtProperty, data: KtorElement?): List<KtorElement> {

        // ex. call.receive<MyRequest>()
        val dotQualified = property.children.filterIsInstance<KtDotQualifiedExpression>()
        // ex. call.request.parameters["query_param"]
        val arrayAccess = property.children.filterIsInstance<KtArrayAccessExpression>()
        return dotQualified.plus(arrayAccess).flatMap { it.accept(this, data) ?: emptyList() }
    }

    override fun visitArrayAccessExpression(
        expression: KtArrayAccessExpression,
        parent: KtorElement?
    ): List<KtorElement> {

        if (expression.isKtorQueryParam() && parent is EndPoint) {

            val queryParamValue = resolveArrayAccessIndex(expression, this@ExpressionVisitor.context)

            if (queryParamValue != null) {
                parent.queryParameters = parent.queryParameters merge listOf(queryParamValue)
            }
        }

        return parent?.let { listOf(it) } ?: emptyList()
    }

    override fun visitDeclaration(dcl: KtDeclaration, data: KtorElement?): List<KtorElement> {
        return if (dcl is KtNamedFunction) {
            dcl.bodyExpression?.accept(this, null) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun visitBlockExpression(
        expression: KtBlockExpression,
        parent: KtorElement?
    ): List<KtorElement> {
        println("visitBlockExpression $parent")

        return if (parent is EndPoint) {
            expression.statements
                .flatMap {
                    it?.accept(this, parent) ?: emptyList()
                }
        } else {
            val results = expression.statements
                .filter { it is KtCallExpression || it is KtDotQualifiedExpression || it is KtAnnotatedExpression }
                .also { println(it.size) }
                .flatMap {
                    it.accept(this, parent)
                }

            results
        }
    }

    private fun KtExpression.findCallExpression(): KtExpression? {
        val firstChild = firstChild as? KtExpression
        return if (firstChild?.text != "call") {
            firstChild?.findCallExpression()
        } else {
            firstChild.parent as KtExpression
        }
    }

    private fun KotlinType?.isPrimitiveOrString(): Boolean {
        if (this == null) return false
        return KotlinBuiltIns.isPrimitiveType(this) || KotlinBuiltIns.isString(this)
    }

    private fun KtDotQualifiedExpression.isKtorApplicationCall(): Boolean {
        return (children
            .firstIsInstanceOrNull<KtReferenceExpression>()
            ?.getType(this@ExpressionVisitor.context)
            ?.getKotlinTypeFqName(false)
            ?.endsWith("ApplicationCall") == true) ||
                (children
                    .firstIsInstanceOrNull<KtDotQualifiedExpression>()
                    ?.receiverExpression
                    ?.getType(this@ExpressionVisitor.context)
                    ?.getKotlinTypeFqName(false)
                    ?.endsWith("ApplicationCall") == true) || (
                children.firstIsInstanceOrNull<KtArrayAccessExpression>()
                    ?.children
                    ?.firstIsInstanceOrNull<KtDotQualifiedExpression>()
                    ?.getType(this@ExpressionVisitor.context)
                    ?.getKotlinTypeFqName(false)
                    ?.endsWith("io.ktor.http.Parameters") == true
                )

    }

    private fun resolveArrayAccessIndex(
        arrayAccessExpression: KtArrayAccessExpression?,
        bindingContext: BindingContext
    ): String? {
        if (arrayAccessExpression == null) return null
        val indices = arrayAccessExpression.indexExpressions
        for (indexExpression in indices) {
            when (indexExpression) {
                is KtReferenceExpression -> {
                    val resolvedCall = indexExpression.getResolvedCall(bindingContext)
                    val resultingDescriptor = resolvedCall?.resultingDescriptor

                    if (resultingDescriptor is PropertyDescriptor) {
                        return resultingDescriptor.compileTimeInitializer?.value?.toString()
                    }
                }

                is KtStringTemplateExpression -> {

                    return indexExpression.entries.fold("") { acc, next ->
                        var r = acc
                        when (next) {
                            is KtLiteralStringTemplateEntry -> {
                                r = acc + next.text
                            }

                            is KtBlockStringTemplateEntry -> {
                                val reference = next.children
                                    .firstIsInstanceOrNull<KtReferenceExpression>()
                                    .getResolvedCall(bindingContext)?.resultingDescriptor

                                val dotQualifiedReference = next.children
                                    .firstIsInstanceOrNull<KtDotQualifiedExpression>()
                                    .getResolvedCall(bindingContext)?.resultingDescriptor

                                val ref = reference ?: dotQualifiedReference

                                if (ref is PropertyDescriptor) {
                                    r = acc + ref.compileTimeInitializer?.value?.toString()
                                }
                            }
                        }
                        r
                    }
                }

                is KtBinaryExpression -> {
                    return evaluateBinaryExpression(indexExpression)
                }

                is KtDotQualifiedExpression -> {
                    val receiverExpression = indexExpression.receiverExpression
                    if (receiverExpression is KtDotQualifiedExpression) {
                        val receiver = receiverExpression.receiverExpression
                        val selector = receiverExpression.selectorExpression

                        if (receiver is KtNameReferenceExpression && selector is KtNameReferenceExpression) {
                            // The left-hand side is an enum class
                            val enumClassDescriptor = bindingContext[BindingContext.REFERENCE_TARGET, receiver]
                            // The right-hand side is an enum entry
                            val enumEntryDescriptor = bindingContext[BindingContext.REFERENCE_TARGET, selector]
                            if (enumClassDescriptor is ClassDescriptor && enumClassDescriptor.kind == ClassKind.ENUM_CLASS
                                && enumEntryDescriptor is ClassDescriptor && enumEntryDescriptor.kind == ClassKind.ENUM_ENTRY
                            ) {
                                val enumEntryName = enumEntryDescriptor.findPsi()
                                    ?.children
                                    ?.filterIsInstance<KtInitializerList>()
                                    ?.firstOrNull()
                                    ?.text
                                    ?.replace("[()\"]".toRegex(), "")

                                if (enumEntryName != null) {
                                    return enumEntryName
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun KtDotQualifiedExpression.deepSearchOfAppCall(): Boolean {
        return if (this.isKtorApplicationCall()) {
            true
        } else if (children.isNotEmpty()) {
            children.firstIsInstanceOrNull<KtDotQualifiedExpression>()?.deepSearchOfAppCall() ?: false
        } else {
            false
        }
    }

    private fun KtExpression.isKtorQueryParam() = text.contains("call.request.queryParameters")
            || text.contains("call.request.rawQueryParameters")

    private fun KtExpression.isKtorReceive() = text.contains("call.receive")
    private fun KtExpression.isKtorRespond() = text.contains("call.respond")
    override fun visitDotQualifiedExpression(
        expression: KtDotQualifiedExpression,
        parent: KtorElement?
    ): List<KtorElement> {

        if (expression.deepSearchOfAppCall()) {

            if (parent is EndPoint) {

                if (expression.isKtorReceive()) {

                    val kotlinType = expression
                        .findCallExpression()
                        ?.getType(context)

                    kotlinType?.let {
                        if (!kotlinType.isPrimitiveOrString()) {
                            if (requestBodyFeature) {
                                parent.body = kotlinType.generateTypeAndVisitMemberDescriptors()
                            }
                        } else {
                            parent.body = ObjectType(type = kotlinType.toString().toSwaggerType())
                        }
                    }

                }

                if (expression.isKtorQueryParam()) {
                    val arrayExpression = expression.children.firstIsInstanceOrNull<KtArrayAccessExpression>()
                    val queryParamValue = resolveArrayAccessIndex(arrayExpression, this@ExpressionVisitor.context)

                    if (queryParamValue != null) {
                        parent.queryParameters = parent.queryParameters merge listOf(queryParamValue)
                    }
                }
            }
        }
        return parent?.let { listOf(it) } ?: emptyList()
    }

    private fun KotlinType.generateTypeAndVisitMemberDescriptors(): ObjectType {
        val jetTypeFqName = getKotlinTypeFqName(false)
        val r = ObjectType(
            type = "object",
            properties = mutableMapOf(),
            fqName = jetTypeFqName,
            contentBodyRef = "#/components/schemas/${jetTypeFqName}",
        )
        if (!classNames.names.contains(jetTypeFqName)) {
            classNames.add(r)
            memberScope
                .forEachVariable { d ->
                    val classDescriptorVisitor = ClassDescriptorVisitor(context)
                    d.accept(classDescriptorVisitor, r)
                    classNames.addAll(classDescriptorVisitor.classNames)
                }
        }
        return r
    }


    private fun ResolvedCall<out CallableDescriptor>?.isARouteDefinition(): Boolean {

        val doesExtendRoute = this?.resultingDescriptor
            ?.extensionReceiverParameter
            ?.type
            ?.getKotlinTypeFqName(false)?.contains("Route") == true

        val doesContainPathValue = this?.valueArguments
            ?.keys
            ?.map { it.name.toString() }
            ?.contains("path") == true

        return doesExtendRoute && doesContainPathValue

    }

    override fun visitAnnotatedExpression(exp: KtAnnotatedExpression, parent: KtorElement?): List<KtorElement> {

        println("visitAnnotatedExpression $parent")

        if (exp.baseExpression is KtCallExpression) {
            val expression = exp.baseExpression as KtCallExpression
            val expName = expression.getCallNameExpression()?.text.toString() ?: ""
            var resultElement: KtorElement? = parent

            val resolvedExp = expression.getResolvedCall(context)

            if (resolvedExp?.isARouteDefinition() == true || ExpType.METHOD.labels.contains(expName)) {

                val pathKey = resolvedExp?.valueArguments?.keys?.find { it.name.toString().contains("path") }

                val routePathArg = resolvedExp?.valueArguments?.get(pathKey)
                    ?.arguments
                    ?.firstOrNull()
                    ?.getArgumentExpression()
                    ?.text
                    ?.replace("\"", "")

                if (ExpType.ROUTE.labels.contains(expName)) {
                    if (parent == null) {
                        println("Adding new route")
                        resultElement = routePathArg?.let {
                            DocRoute(routePathArg)
                        } ?: run {
                            DocRoute(expName)
                        }
                    } else {
                        if (parent is DocRoute) {
                            val newElement = DocRoute(routePathArg.toString())
                            resultElement = newElement
                            parent.children.add(newElement)
                        }
                    }
                } else if (ExpType.METHOD.labels.contains(expName)) {

                    val (summary, descr) = expression.findDocsDescription(context)
                    if (parent == null) {
                        resultElement = routePathArg?.let {
                            EndPoint(routePathArg, expName, description = descr, summary = summary)
                        } ?: EndPoint(expName, description = descr, summary = summary)
                    } else {
                        if (parent is DocRoute) {
                            resultElement = EndPoint(routePathArg, expName, description = descr, summary = summary)
                            parent.children.add(resultElement)
                        } else {
                            throw IllegalArgumentException("Endpoints cant have Endpoint as routes")
                        }
                    }
                } else if (parent == null) {
                    resultElement = DocRoute()
                }
            }

            /*
                    if (resolvedExp
                            ?.resultingDescriptor
                            ?.explicitParameters
                            ?.first()?.type
                            ?.getJetTypeFqName(false)
                            ?.contains("ApplicationCall") == true
                    ) {

                        val resolvedArguments = resolvedExp.valueArguments

                        val messageParamDesc = resolvedArguments.keys
                            .find { it.name.asString() == "message" }

                        val statusParamDesc = resolvedArguments.keys
                            .find { it.name.asString() == "status" }

                        if (messageParamDesc != null && statusParamDesc !== null && parent is EndPoint) {

                            val statusParamValue = resolvedArguments.get(statusParamDesc)
                                ?.arguments?.first()
                                ?.getArgumentExpression()

                            val statusCode = HttpCodeResolver.resolve(statusParamValue?.getCall(context)?.callElement?.text)

                            val kotlinType = resolvedArguments.get(messageParamDesc)
                                ?.arguments?.firstOrNull()
                                ?.getArgumentExpression()
                                ?.getType(context)

                            kotlinType?.let {
                                if (!kotlinType.isPrimitiveOrString()) {

                                    val jetTypeFqName = kotlinType.getJetTypeFqName(false)

                                    if (requestBodyFeature) {
                                        kotlinType.generateTypeAndVisitMemberDescriptors()

            //                            parent.responses = OpenApiSpec.Response(
            //                                mapOf(
            //                                    statusCode to OpenApiSpec.ResponseDetails(
            //                                        "",
            //                                        OpenApiSpec.Schema("object", jetTypeFqName)
            //                                    )
            //                                )
            //                            )
                                    }
                                } else {
            //                        parent.responses = OpenApiSpec.Response(
            //                            mapOf(
            //                                statusCode to OpenApiSpec.ResponseDetails(
            //                                    "",
            //                                    OpenApiSpec.Schema(
            //                                        kotlinType.toString().toSwaggerType(),
            //                                        null
            //                                    )
            //                                )
            //                            )
            //                        )
                                }
                            }
                        }
                    }

            */
            val lambda = expression.lambdaArguments.lastOrNull()
            if (lambda != null) { // visiting lambda of call expression

                //res is children?
                val res = lambda.getLambdaExpression()?.accept(this, resultElement)

                return resultElement.wrapAsList().ifEmpty {
                    res.orEmpty()
                }

            } else { // the expression doesn't have lambda so visit its implementation to looks for potential routes
                val res = expression.visitExpressionDefinition(this, resultElement)
                return resultElement.wrapAsList().ifEmpty {
                    res.orEmpty()
                }

            }
        } else {
            return super.visitAnnotatedExpression(exp, parent)
        }
    }

    override fun visitCallExpression(
        expression: KtCallExpression,
        parent: KtorElement?
    ): List<KtorElement> {

        println("visitCallExpression $parent")
        val expName = expression.getCallNameExpression()?.text.toString()
        var resultElement: KtorElement? = parent

        val resolvedExp = expression.getResolvedCall(context)

        if (resolvedExp?.isARouteDefinition() == true || ExpType.METHOD.labels.contains(expName)) {

            val pathKey = resolvedExp?.valueArguments?.keys?.find { it.name.toString().contains("path") }

            val routePathArg = resolvedExp?.valueArguments?.get(pathKey)
                ?.arguments
                ?.firstOrNull()
                ?.getArgumentExpression()
                ?.text
                ?.replace("\"", "")

            if (ExpType.ROUTE.labels.contains(expName)) {
                if (parent == null) {
                    println("Adding new route")
                    resultElement = routePathArg?.let {
                        DocRoute(routePathArg)
                    } ?: run {
                        DocRoute(expName)
                    }
                } else {
                    if (parent is DocRoute) {
                        val newElement = DocRoute(routePathArg.toString())
                        resultElement = newElement
                        parent.children.add(newElement)
                    }
                }
            } else if (ExpType.METHOD.labels.contains(expName)) {
                if (parent == null) {
                    resultElement = routePathArg?.let {
                        EndPoint(routePathArg, expName)
                    } ?: EndPoint(expName)
                } else {
                    if (parent is DocRoute) {
                        resultElement = EndPoint(routePathArg, expName)
                        parent.children.add(resultElement)
                    } else {
                        throw IllegalArgumentException("Endpoints cant have Endpoint as routes")
                    }
                }
            } else if (parent == null) {
                resultElement = DocRoute()
            }
        }
        val lambda = expression.lambdaArguments.lastOrNull()
        if (lambda != null) { // visiting lambda of call expression
            val res = lambda.getLambdaExpression()?.accept(this, resultElement)

            return resultElement.wrapAsList().ifEmpty {
                res.orEmpty()
            }

        } else { // the expression doesn't have lambda so visit its implementation to looks for potential routes
            val res = expression.visitExpressionDefinition(this, resultElement)
            return resultElement.wrapAsList().ifEmpty {
                res.orEmpty()
            }

        }
    }
    private fun KtorElement?.wrapAsList() = this?.let { listOf(this) } ?: emptyList()
    private fun KtCallExpression.visitExpressionDefinition(
        expressionVisitor: ExpressionVisitor,
        kontext: KtorElement?
    ): List<KtorElement>? {

        val expName = getCallNameExpression()?.text.toString()

        return calleeExpression?.let { exp ->

            if (exp.isInImportDirective()) {

                null

            } else {
                exp.containingKtFile.children
                    .filterIsInstance<KtNamedFunction>()
                    .firstOrNull { it.name == expName }?.bodyExpression?.accept(expressionVisitor, kontext)
            }
        }
    }

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression, parent: KtorElement?): List<KtorElement> {
        println("visitLambdaExpression $parent")
        return lambdaExpression.bodyExpression?.accept(this, parent) ?: parent.wrapAsList()
    }

}

@OptIn(UnsafeCastFunction::class)
private fun KtExpression.findDocsDescription(context: BindingContext): Pair<String?, String?> {

    return getAnnotationEntries().find { it ->
        it.typeReference?.typeElement.safeAs<KtUserType>()?.referencedName == KtorDescription::class.simpleName!!
    }?.let { annotationDescriptor ->

        var summary = annotationDescriptor.valueArguments.find { it.getArgumentName()?.asName?.asString() == "summary" }

        if (summary == null) {
            summary = annotationDescriptor.valueArgumentList?.arguments?.find { it.argumentIndex == 0 }
        }

        var desc =
            annotationDescriptor.valueArguments.find { it.getArgumentName()?.asName?.asString() == "description" }

        if (desc == null) {
            desc = annotationDescriptor.valueArgumentList?.arguments?.find { it.argumentIndex == 1 }
        }

        summary?.getArgumentExpression().evaluateStringConcatenation() to desc?.getArgumentExpression()
            .evaluateStringConcatenation()
    } ?: ("" to "")

}

fun evaluateBinaryExpression(expression: KtBinaryExpression): String {
    val leftExpression = expression.left
    val rightExpression = expression.right

    val leftValue = when (leftExpression) {
        is KtBinaryExpression -> evaluateBinaryExpression(leftExpression)
        is KtStringTemplateExpression -> leftExpression.node.text.replace("\"", "")
        else -> leftExpression?.text?.replace("\"", "")
    }

    val rightValue = when (rightExpression) {
        is KtBinaryExpression -> evaluateBinaryExpression(rightExpression)
        is KtStringTemplateExpression -> rightExpression.node.text.replace("\"", "")
        else -> rightExpression?.text?.replace("\"", "")
    }

    return leftValue + rightValue
}

fun KtExpression?.evaluateStringConcatenation(): String {
    return when (this) {
        is KtStringTemplateExpression -> {
            entries.joinToString(separator = "") { entry ->
                when (entry) {
                    is KtStringTemplateEntryWithExpression -> entry.expression!!.evaluateStringConcatenation()
                    else -> entry.text
                }
            }
        }

        is KtBinaryExpression -> evaluateBinaryExpression(this)
        else -> this?.text ?: ""
    }
}

val ClassDescriptor.enumEntries: Set<ClassDescriptor>
    get() = DescriptorUtils.getAllDescriptors(this.unsubstitutedInnerClassesScope)
        .filter(DescriptorUtils::isEnumEntry)
        .filterIsInstance<ClassDescriptor>()
        .toSet()

