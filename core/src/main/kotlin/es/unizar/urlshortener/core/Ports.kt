package es.unizar.urlshortener.core
import java.time.OffsetDateTime

/**
 * [ClickRepositoryService] is the port to the repository that provides persistence to [Clicks][Click].
 */
interface ClickRepositoryService {
    /**
     * Saves a [Click] entity to the repository.
     *
     * @param cl The [Click] entity to be saved.
     * @return The saved [Click] entity.
     */
    fun save(cl: Click): Click
    /**
     * Counts the number of clicks in a given time range for a specific URL.
     *
     * @param hash The hash of the URL to count clicks for.
     * @param currentTime The current time (end of the time range).
     * @param startTime The start time of the period in which to count clicks.
     * @return The number of clicks in the specified time range.
     */
    fun countClicksInTimeRange(hash: String, currentTime: OffsetDateTime, startTime: OffsetDateTime): Long
}

/**
 * [ShortUrlRepositoryService] is the port to the repository that provides management to [ShortUrl][ShortUrl].
 */
interface ShortUrlRepositoryService {
    /**
     * Finds a [ShortUrl] entity by its key.
     *
     * @param id The key of the [ShortUrl] entity.
     * @return The found [ShortUrl] entity or null if not found.
     */
    fun findByKey(id: String): ShortUrl?

    /**
     * Saves a [ShortUrl] entity to the repository.
     *
     * @param su The [ShortUrl] entity to be saved.
     * @return The saved [ShortUrl] entity.
     */
    fun save(su: ShortUrl): ShortUrl
}

/**
 * [ValidatorService] is the port to the service that validates if an url can be shortened.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface ValidatorService {
    /**
     * Validates if the given URL can be shortened.
     *
     * @param url The URL to be validated.
     * @return True if the URL is valid, false otherwise.
     */
    suspend fun isValid(url: String): Boolean
}

/**
 * [HashService] is the port to the service that creates a hash from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface HashService {
    /**
     * Creates a hash from the given URL.
     *
     * @param url The URL to be hashed.
     * @return The hash of the URL.
     */
    fun hasUrl(url: String): String
}

/**
 * [SafeUrlService] is the port to the service that checks if an URL is safe.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */

interface SafeUrlService {
    /**
     * Checks if the given URL is safe.
     *
     * @param url The URL to be checked.
     * @return True if the URL is safe, false otherwise.
     */
    fun isSafe(url: String): Boolean
}
