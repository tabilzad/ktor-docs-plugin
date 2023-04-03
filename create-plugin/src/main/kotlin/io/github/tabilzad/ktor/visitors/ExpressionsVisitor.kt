package io.github.tabilzad.ktor.visitors

import ClassDescriptorVisitor
import io.github.tabilzad.ktor.*
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import toSwaggerType

internal class ExpressionsVisitor(
    private val requestBodyFeature: Boolean,
    private val context: BindingContext
) : KtVisitor<List<KtorElement>, KtorElement?>() {
    init {
        println("BeginVisitor")
    }

    val classNames = mutableListOf<ObjectType>()
    override fun visitProperty(property: KtProperty, data: KtorElement?): List<KtorElement> {
        return property.children.filterIsInstance<KtDotQualifiedExpression>().flatMap { it.accept(this, data) }
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
                .filterIsInstance<KtCallExpression>()
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
            ?.getType(this@ExpressionsVisitor.context)
            ?.getJetTypeFqName(false)
            ?.endsWith("ApplicationCall") == true) ||
                (children
                    .firstIsInstanceOrNull<KtDotQualifiedExpression>()
                    ?.receiverExpression
                    ?.getType(this@ExpressionsVisitor.context)
                    ?.getJetTypeFqName(false)
                    ?.endsWith("ApplicationCall") == true)
    }

    private fun KtDotQualifiedExpression.deepSearchOfAppCall(): Boolean{
        return if(this.isKtorApplicationCall()){
            true
        }else if(children.isNotEmpty()){
            children.firstIsInstanceOrNull<KtDotQualifiedExpression>()?.deepSearchOfAppCall() ?: false
        }else{
            false
        }
    }

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
                            parent.body = ObjectType(kotlinType.toString().toSwaggerType())
                        }
                    }
                }
            }
        }
        return parent?.let { listOf(it) } ?: emptyList()
    }

    private fun KotlinType.generateTypeAndVisitMemberDescriptors(): ObjectType {
        val jetTypeFqName = getJetTypeFqName(false)
        val r = ObjectType(
            "object",
            mutableMapOf(),
            name = jetTypeFqName
        )
        if (!classNames.names.contains(jetTypeFqName)) {
            classNames.add(r)
            memberScope
                .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                .forEach { d ->
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
            ?.getJetTypeFqName(false)?.contains("Route") == true

        val doesContainPathValue = this?.valueArguments
            ?.keys
            ?.map { it.name.toString() }
            ?.contains("path") == true

        return doesExtendRoute && doesContainPathValue

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
        visitor: ExpressionsVisitor,
        kontext: KtorElement?
    ): List<KtorElement>? {

        val expName = getCallNameExpression()?.text.toString()

        return calleeExpression?.let { exp ->

            if (exp.isInImportDirective()) {

                null

            } else {
                exp.containingKtFile.children
                    .filterIsInstance<KtNamedFunction>()
                    .firstOrNull { it.name == expName }?.bodyExpression?.accept(visitor, kontext)
            }
        }
    }


    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression, parent: KtorElement?): List<KtorElement> {
        println("visitLambdaExpression $parent")
        return lambdaExpression.bodyExpression?.accept(this, parent) ?: parent.wrapAsList()
    }

}