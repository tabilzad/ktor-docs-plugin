package io.github.tabilzad.ktor

// Internal
internal data class PluginConfiguration(
    val isEnabled: Boolean,
    val format: String,
    val title: String,
    val description: String,
    val version: String,
    val filePath: String,
    val requestBody: Boolean,
    val hideTransients: Boolean,
    val hidePrivateFields: Boolean,
    val deriveFieldRequirementFromTypeNullability: Boolean
)
