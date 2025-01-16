package io.github.tabilzad.ktor.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class KtorSecurity(
    val schemes: Array<SecurityScheme> = []
)

annotation class SecurityScheme(
    val name: String,             // Name of the security scheme
    val type: SecurityType,       // Type of the scheme (e.g., API key, OAuth2, HTTP)
    val description: String = ""  // Optional description of the security scheme
)

enum class SecurityType {
    API_KEY,    // For API key-based authentication
    OAUTH2,     // For OAuth2-based authentication
    HTTP,       // For HTTP-based authentication (e.g., basic auth, bearer token)
    OPENID_CONNECT // For OpenID Connect
}