package io.github.tabilzad.ktor.k1

import io.github.tabilzad.ktor.PluginConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

// Registers K1 component
internal class DeclarationExtension(
    private val configuration: PluginConfiguration
) : StorageComponentContainerContributor {


    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: TargetPlatform,
        moduleDescriptor: ModuleDescriptor
    ) {

        container.useInstance(object : DeclarationChecker {
            override fun check(
                declaration: KtDeclaration,
                descriptor: DeclarationDescriptor,
                context: DeclarationCheckerContext
            ) {
                swaggerExtensionPhase(configuration, declaration, descriptor, context)
            }
        })
    }
}
