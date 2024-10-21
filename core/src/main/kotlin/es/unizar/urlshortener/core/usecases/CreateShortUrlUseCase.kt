@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
    // Inicia una coroutine para manejar la validación asincrónicamente
        GlobalScope.launch {
            // Validar la URL de manera asincrónica
            val isValid = validatorService.isValid(url)
            
            if (!isValid) {
                throw InvalidUrlException(url)
            }
        }

        // Generate the hash for the URL
        val id = safeCall { hashService.hasUrl(url) }

        // Check if the URL is safe
        val isSafeUrl = safeCall { safeUrlService.isSafe(url) }

        // Create the ShortUrl entity
        val shortUrl = ShortUrl(
            hash = id,
            redirection = Redirection(target = url),
            properties = ShortUrlProperties(
                safe = isSafeUrl,
                ip = data.ip, // Ensure nullability is handled appropriately
                sponsor = data.sponsor
            ),
            validationStatus = ValidationStatus.PENDING 
        )

        // Validar la URL de manera asincrónica con una coroutine
        GlobalScope.launch {
            validateUrlAsync(url, id)
        }

        // Save the ShortUrl entity
        return safeCall { shortUrlRepository.save(shortUrl) }
    }


    private suspend fun validateUrlAsync(url: String, hash: String) {
        try {
            val isValid = validatorService.isValid(url)
            val validationStatus = if (isValid) ValidationStatus.REACHABLE else ValidationStatus.UNREACHABLE

            // Actualizar la URL con el estado de validación
            val shortUrl = shortUrlRepository.findByKey(hash)
            shortUrl?.let {
                it.validationStatus = validationStatus
                shortUrlRepository.save(it)
            }
        } catch (e: Exception) {
            // Si falla la validación, marcar como no alcanzable
            val shortUrl = shortUrlRepository.findByKey(hash)
            shortUrl?.let {
                it.validationStatus = ValidationStatus.UNREACHABLE
                shortUrlRepository.save(it)
            }
        }
    }

}

