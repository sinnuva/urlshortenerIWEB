@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     */
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService,
    private val safeUrlService: SafeUrlService
) : CreateShortUrlUseCase {

    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     * @throws InvalidUrlException if the URL is not valid.
     */
    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        if (safeCall { validatorService.isValid(url) }) {
            // Generate the hash for the URL
            val id = safeCall { hashService.hasUrl(url) }

            // Check if the URL is safe
            val isSafeUrl = safeCall { safeUrlService.isSafe(url) }

            // Create the ShortUrl entity
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = isSafeUrl,
                    ip = data.ip,
                    sponsor = data.sponsor
                )
            )
            // Save the ShortUrl entity
            safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(url)
        }
}

