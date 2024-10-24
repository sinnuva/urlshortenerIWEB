@file:Suppress("MatchingDeclarationName", "WildcardImport")

package es.unizar.urlshortener

import es.unizar.urlshortener.infrastructure.delivery.ShortUrlDataOut
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import es.unizar.urlshortener.core.ValidatorService
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * Integration tests for HTTP requests.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpRequestTest {
    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Autowired
    private lateinit var validatorService: ValidatorService

    /**
     * Sets up the test environment before each test.
     * Configures the HTTP client to disable redirect handling and clears the database tables.
     */
    @BeforeTest
    fun setup() {
        val httpClient = HttpClientBuilder.create()
            .disableRedirectHandling()
            .build()
        (restTemplate.restTemplate.requestFactory as HttpComponentsClientHttpRequestFactory).httpClient = httpClient

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "shorturl", "click")
    }

    /**
     * Cleans up the test environment after each test.
     * Clears the database tables.
     */
    @AfterTest
    fun tearDowns() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "shorturl", "click")
    }

    /**
     * Tests that the main page is accessible and contains the expected content.
     */
    @Test
    fun `main page works`() {
        val response = restTemplate.getForEntity("http://localhost:$port/", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("A front-end example page for the project")
    }

    /**
     * Tests that a redirect is returned when the key exists.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        // Assuming the short URL creation is necessary before testing redirect
        val shortUrlResponse = shortUrl("http://example.com/")
        val target = shortUrlResponse.headers.location
        require(target != null) { "Target location should not be null after creating a short URL." }

        // Debugging output
        println("Created Short URL: $target")

        val response = restTemplate.getForEntity(target, String::class.java)

        // Debugging output
        println("Redirect Response Status: ${response.statusCode}")
        println("Redirect Response Location: ${response.headers.location}")

        assertThat(response.statusCode).isEqualTo(HttpStatus.TEMPORARY_REDIRECT)
        assertThat(response.headers.location).isEqualTo(URI.create("http://example.com/"))

        // Debugging the database state
        val clickCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")
        println("Rows in 'click' table after redirect: $clickCount")
        assertThat(clickCount).isEqualTo(1)
    }

    /**
     * Tests that a not found status is returned when the key does not exist.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        val response = restTemplate.getForEntity("http://localhost:$port/f684a3c4", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    /**
     * Tests that a basic redirect is created if a hash can be computed.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        val response = shortUrl("http://example.com/")

        // Debugging output
        println("Response Status: ${response.statusCode}")
        println("Response Location: ${response.headers.location}")
        println("Response Body: ${response.body}")

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.headers.location).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))
        assertThat(response.body?.url).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))

        // Debugging the database state
        val shortUrlCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")
        println("Rows in 'shorturl' table: $shortUrlCount")
        assertThat(shortUrlCount).isEqualTo(1)

        val clickCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")
        println("Rows in 'click' table: $clickCount")
        assertThat(clickCount).isEqualTo(0)
    }

    /**
     * Tests that a bad request status is returned if a hash cannot be computed.
     */
    @Test
    fun `creates returns bad request if it can't compute a hash`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val data: MultiValueMap<String, String> = LinkedMultiValueMap()
        data["url"] = "ftp://example.com/"

        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/link",
            HttpEntity(data, headers),
            ShortUrlDataOut::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")).isEqualTo(0)
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }
    @Test
    fun `creates a malicious URL`() {
        val response = shortUrl("http://testsafebrowsing.appspot.com/s/malware.html")

        // Debugging: Print the entire response
        println("Response body: ${response.body}")

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // Extract the 'safe' property from the map
        val safeValue = response.body?.properties?.get("safe") as? Boolean
        println("Safe property value: $safeValue")

        // Assert that the 'safe' property is false for the malicious URL
        assertThat(safeValue).isEqualTo(false)

        // Database checks
        val shortUrlCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")
        println("Rows in 'shorturl' table: $shortUrlCount")
        assertThat(shortUrlCount).isEqualTo(1)

        val clickCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")
        println("Rows in 'click' table: $clickCount")
        assertThat(clickCount).isEqualTo(0)
    }

    @Test
    fun `should return true for a reachable URL`() {
        val result = validatorService.isValid("https://www.google.com")
        assertTrue(result)
    }

    @Test
    fun `should return false for an unreachable URL`() {
        val result = validatorService.isValid("http://invalid-url.com")
        assertFalse(result)
    }

    /**
     * Creates a short URL for the given URL.
     * @param url The URL to shorten.
     * @return The response entity containing the short URL data.
     */
    private fun shortUrl(url: String): ResponseEntity<ShortUrlDataOut> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val data: MultiValueMap<String, String> = LinkedMultiValueMap()
        data["url"] = url

        return restTemplate.postForEntity(
            "http://localhost:$port/api/link",
            HttpEntity(data, headers),
            ShortUrlDataOut::class.java
        )
    }

    @Test
    fun shouldReturnValidQRCode() {
        val response = shortUrl("http://example.com/")

        // Debugging output
        println("Response Status: ${response.statusCode}")
        println("Response Location: ${response.headers.location}")
        println("Response Body: ${response.body}")

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.headers.location).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))
        assertThat(response.body?.url).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))
        assertThat(response.body?.qrCode).isNotNull()

        // Verify that the qrCode starts with the correct prefix
        val qrCodeDataUrl = response.body?.qrCode
        assertTrue(qrCodeDataUrl!!.startsWith("data:image/png;base64,"))

        // Decode the base64 string
        val base64Image = qrCodeDataUrl.removePrefix("data:image/png;base64,")
        val imageBytes = Base64.getDecoder().decode(base64Image)
        assertThat(imageBytes).isNotEmpty

        // Read the image bytes to ensure it's a valid image
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assertThat(bufferedImage).isNotNull
        assertThat(bufferedImage.width).isGreaterThan(0)
        assertThat(bufferedImage.height).isGreaterThan(0)
    }
}
