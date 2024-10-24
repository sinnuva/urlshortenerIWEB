@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.slf4j.LoggerFactory
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*
import kotlin.test.Test

@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerImpl::class,
        RestResponseEntityExceptionHandler::class
    ]
)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    /**
     * Tests that `redirectTo` returns a redirect when the key exists.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        // Mock the behavior of redirectUseCase to return a redirection URL
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))

        // Perform a GET request and verify the response status and redirection URL
        mockMvc.perform(get("/{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        // Verify that logClickUseCase logs the click with the correct IP address
        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    /**
     * Tests that `redirectTo` returns a not found status when the key does not exist.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        // Mock the behavior of redirectUseCase to throw a RedirectionNotFound exception
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        // Perform a GET request and verify the response status and error message
        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))

        // Verify that logClickUseCase does not log the click
        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    /**
    * Tests that `redirectTo` returns a 429 Too Many Requests status when the limit is reached.
    */
    @Test
    fun `redirectTo returns too many requests when the redirection limit is reached`() {
        // Mock the behavior of redirectUseCase to throw a TooManyRequestsException
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw TooManyRequestsException("key") }

        // Perform a GET request and verify the response status is 429
        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.statusCode").value(429))

        // Verify that logClickUseCase does not log the click
        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    /**
     * Tests that `creates` returns a basic redirect if it can compute a hash.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        // Mock the behavior of createShortUrlUseCase to return a ShortUrl object
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://example.com/")))

        // Perform a POST request and verify the response status, redirection URL, and JSON response
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }

    /**
     * Tests that `creates` returns a bad request status if it cannot compute a hash.
     */
    @Test
    fun `creates returns bad request if it can compute a hash`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidUrlException
        given(
            createShortUrlUseCase.create(
                url = "ftp://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "ftp://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.statusCode").value(400))
    }

    /**
     * Tests that `shortener` returns a response containing a QR code.
     */
    @Test
    fun `shortener returns QR code in response`() {
        // Given a successful short URL creation
        val hash = "f684a3c4"
        val targetUrl = "http://example.com/"
        given(
            createShortUrlUseCase.create(
                url = targetUrl,
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willReturn(ShortUrl(hash, Redirection(targetUrl)))

        // When posting to /api/link
        val result = mockMvc.perform(
            post("/api/link")
                .param("url", targetUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            // Then expect a 201 Created status and the QR code in the response
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.qrCode").exists())
            .andExpect(jsonPath("$.url").value("http://localhost/$hash"))
            .andReturn()

        // Extract the QR code from the response and verify it's a valid base64 string
        val jsonResponse = result.response.contentAsString
        val qrCodeBase64 = extractJsonValue(jsonResponse, "qrCode")
        assert(isValidBase64Image(qrCodeBase64)) { "Invalid base64 image data" }
    }

    // Helper function to extract a value from JSON response
    private fun extractJsonValue(json: String, key: String): String {
        val regex = """"$key"\s*:\s*"(.*?)"""".toRegex()
        val matchResult = regex.find(json)
        return matchResult?.groups?.get(1)?.value ?: ""
    }

    // Helper function to validate base64 image data
    private val logger = LoggerFactory.getLogger(UrlShortenerControllerTest::class.java)

    private fun isValidBase64Image(base64Data: String): Boolean {
        return try {
            val base64String = base64Data.substringAfter(",")
            val decodedBytes = Base64.getDecoder().decode(base64String)
            decodedBytes.isNotEmpty()
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid Base64 data", e)
            false
        }
    }
}
