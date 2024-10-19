package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.TooManyRequestsException
import java.time.OffsetDateTime
import es.unizar.urlshortener.core.safeCall

/**
 * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
 * and an [HTTP redirection mode][Redirection.mode].
 *
 * **Note**: This is an example of functionality.
 */
interface RedirectUseCase {
    /**
     * Redirects to the target URL associated with the given key.
     *
     * @param key The key associated with the target URL.
     * @return The [Redirection] containing the target URL and redirection mode.
     * @throws RedirectionNotFound if no redirection is found for the given key.
     * @throws TooManyRequestsException if the redirection limit is exceeded.
     */
    fun redirectTo(key: String): Redirection
}

/**
 * Implementation of [RedirectUseCase].
 */
class RedirectUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val clickRepositoryService: ClickRepositoryService  // Used to count clicks, not to save them
) : RedirectUseCase {

    private val MAX_REDIRECTIONS = 5
    private val PERIOD_IN_MINUTES = 1

    /**
     * Redirects to the target URL associated with the given key.
     *
     * @param key The key (hash) of the short URL.
     * @return The [Redirection] containing the target URL and redirection mode.
     * @throws RedirectionNotFound if no redirection is found for the given key.
     * @throws TooManyRequestsException if the redirection limit is exceeded.
     */
    override fun redirectTo(key: String): Redirection = safeCall {
        val currentTime = OffsetDateTime.now()
        val startTime = currentTime.minusMinutes(PERIOD_IN_MINUTES.toLong())

        val recentClicks = clickRepositoryService.countClicksInTimeRange(key, currentTime, startTime)

        if (recentClicks >= MAX_REDIRECTIONS.toLong()) {
            throw TooManyRequestsException(key)
        }

        val shortUrl = shortUrlRepository.findByKey(key) ?: throw RedirectionNotFound(key)

        return@safeCall shortUrl.redirection
    } ?: throw RedirectionNotFound(key)
}