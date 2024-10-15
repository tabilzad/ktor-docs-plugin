package io.github.tabilzad.ktor.k2.visitors

import io.github.tabilzad.ktor.k2.ClassIds
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor

class HeaderParamsVisitor(private val session: FirSession) : FirDefaultVisitor<Unit, MutableList<String>>() {

    override fun visitElement(element: FirElement, data: MutableList<String>) {
        // no-op
    }

    override fun visitStringConcatenationCall(
        stringConcatenationCall: FirStringConcatenationCall,
        data: MutableList<String>
    ) {
        data.add(stringConcatenationCall.argumentList.arguments.flatMap { acc ->
            buildList {
                acc.accept(this@HeaderParamsVisitor, this)
            }
        }.joinToString(""))
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall, data: MutableList<String>) {
        // TODO not sure about naming here
        val getFunctionFqName = functionCall.dispatchReceiver?.toResolvedCallableSymbol(session)?.callableId?.asSingleFqName()
        if (getFunctionFqName == ClassIds.KTOR_APPREQUEST_HEADER) { functionCall.acceptChildren(this, data) }

        val functionFqName = functionCall.calleeReference.toResolvedCallableSymbol()?.callableId?.asSingleFqName()
        if (functionFqName == ClassIds.KTOR_REQUEST_HEADER) { functionCall.acceptChildren(this, data) }

        // else skip
    }

    override fun visitLiteralExpression(literalExpression: FirLiteralExpression, data: MutableList<String>) {
        val element = literalExpression.value
        element?.let { data.add(it.toString()) }
    }

    @OptIn(SymbolInternals::class)
    override fun visitResolvedNamedReference(
        resolvedNamedReference: FirResolvedNamedReference,
        data: MutableList<String>
    ) {
        val fir = resolvedNamedReference.resolvedSymbol.fir
        if (fir is FirProperty) {
            val init = fir.initializer

            if (init is FirLiteralExpression) {
                init.accept(this, data)
            }
        }
    }

    @OptIn(PrivateConstantEvaluatorAPI::class)
    override fun visitArgumentList(argumentList: FirArgumentList, data: MutableList<String>) {
        visitArgumentList(session, argumentList, data)
    }

    @OptIn(SymbolInternals::class)
    override fun visitPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: MutableList<String>
    ) {
        visitPropertyAccessExpression(session, propertyAccessExpression, data)
    }
}
