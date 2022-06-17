package io.github.tabilzad.ktor.visitors

import io.github.tabilzad.ktor.*
import io.github.tabilzad.ktor.OpenApiSpec.ObjectType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

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
                .flatMap { it.accept(this, parent) }
        } else {
            expression.statements
                .filterIsInstance<KtCallExpression>()
                .also { println(it.size) }
                .flatMap {
                    it.accept(this, parent)
                }
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

    override fun visitDotQualifiedExpression(
        expression: KtDotQualifiedExpression,
        parent: KtorElement?
    ): List<KtorElement> {
        val isCall = expression.text.contains("call.receive")
        if (isCall) {
            if (parent is EndPoint) {
                val kotlinType = BindingContextUtils.getTypeNotNull(
                    context,
                    expression.receiverExpression.findCallExpression() ?: expression
                )
                if (!(KotlinBuiltIns.isPrimitiveType(kotlinType) || KotlinBuiltIns.isString(kotlinType))) {

                    val jetTypeFqName = kotlinType.getJetTypeFqName(false)
                    val r = ObjectType(
                        "object",
                        mutableMapOf(),
                        name = jetTypeFqName
                    )

                    if (!classNames.names.contains(jetTypeFqName)) {

                        if (requestBodyFeature) {
                            classNames.add(r)
                            kotlinType.memberScope
                                .getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                                .forEach { d ->
                                    val classDescriptorVisitor = ClassDescriptorVisitor(context)
                                    d.accept(classDescriptorVisitor, r)
                                    classNames.addAll(classDescriptorVisitor.classNames)
                                }

                            parent.body = r
                        }
                    }
                } else {
                    parent.body = ObjectType(kotlinType.toString().toSwaggerType())
                }
            }
        }
        return parent?.let { listOf(it) } ?: emptyList()
    }

    override fun visitCallExpression(
        expression: KtCallExpression,
        parent: KtorElement?
    ): List<KtorElement> {

        println("visitCallExpression $parent")
        val expName = expression.getCallNameExpression()?.text.toString()
        var resultElement: KtorElement? = parent
        val routePathArg =
            expression.valueArguments.firstOrNull { it !is KtLambdaArgument }?.text?.replace("\"", "")
        if (ExpType.ROUTE.labels.contains(expName)) {
            if (parent == null) {
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
        } else {
            if (ExpType.METHOD.labels.contains(expName)) {
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
            }
        }
        val lambda = expression.valueArguments.filterIsInstance<KtLambdaArgument>().firstOrNull()
        lambda?.getLambdaExpression()?.accept(this, resultElement)
        return if (resultElement != null) {
            listOf(resultElement)
        } else {
            emptyList()
        }
    }

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression, parent: KtorElement?): List<KtorElement> {
        println("visitLambdaExpression $parent")
        return lambdaExpression.bodyExpression?.accept(this, parent)
            ?: parent?.let { listOf(parent) } ?: emptyList()
    }

}