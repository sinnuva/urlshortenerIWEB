package es.unizar.urlshortener.core

import java.lang.RuntimeException

/**
 * A base class for domain-specific exceptions in the application.
 * This sealed class serves as a root for all exceptions related to the domain logic.
 * It extends [RuntimeException], allowing for an optional [cause].
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception, which can be null.
 */
sealed class DomainException(message: String, cause: Throwable? = null):
    RuntimeException(message, cause)

/**
 * An exception indicating that a provided URL does not follow a supported schema.
 * This exception is thrown when the format or schema of a URL does not match the expected pattern.
 *
 * @param url The URL that caused the exception.
 */
class InvalidUrlException(url: String) : DomainException("[$url] does not follow a supported schema")

/**
 * An exception indicating that a redirection key could not be found.
 * This exception is thrown when a specified redirection key does not exist in the system.
 *
 * @param key The redirection key that was not found.
 */
class RedirectionNotFound(key: String) : DomainException("[$key] is not known")

/**
 * An exception thrown when the redirection limit for a given URL is reached.
 * This triggers a HTTP 429 Too Many Requests response.
 *
 * @param key The key of the URL that exceeded the redirection limit.
 */
class TooManyRequestsException(key: String) : DomainException("Redirection limit reached for [$key]")

/**
 * An exception indicating an internal error within the application.
 * This exception can be used to represent unexpected issues that occur within the application,
 * providing both a message and a cause for the error.
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception.
 */
class InternalError(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

inline fun <T> safeCall(
    onFailure: (Throwable) -> Throwable = { e -> InternalError("Unexpected error", e) },
    block: () -> T
): T = runCatching {
    block()
}.fold(
    onSuccess = { it },
    onFailure = { exception ->
        when (exception) {
            is RedirectionNotFound, is TooManyRequestsException -> throw exception
            else -> throw onFailure(exception)
        }
    }
)
