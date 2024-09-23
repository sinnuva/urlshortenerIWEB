package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import org.apache.commons.validator.routines.UrlValidator
import java.nio.charset.StandardCharsets

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    /**
     * Validates the given URL.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, false otherwise
     */
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        /**
         * A URL validator that supports HTTP and HTTPS schemes.
         */
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Implementation of the port [HashService].
 */
class HashServiceImpl : HashService {
    /**
     * Generates a hash for the given URL using the Murmur3 32-bit hashing algorithm.
     *
     * @param url the URL to hash
     * @return the hash of the URL as a string
     */
    override fun hasUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}
