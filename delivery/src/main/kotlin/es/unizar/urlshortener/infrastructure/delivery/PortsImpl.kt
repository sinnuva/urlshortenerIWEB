package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.SafeUrlService
import org.json.JSONObject
import org.apache.commons.validator.routines.UrlValidator
import java.nio.charset.StandardCharsets

//Google API check imports
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import io.github.cdimascio.dotenv.Dotenv
import kotlin.Exception



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

class SafeUrlServiceImpl : SafeUrlService {

    private val dotenv = Dotenv.load()
    private val googleSafeBrowsingApiKey: String? = dotenv["GOOGLE_SAFE_BROWSING_API_KEY"]

    override fun isSafe(input: String): Boolean {
    // Check if the API key is valid
    require(!googleSafeBrowsingApiKey.isNullOrEmpty()) { "Google Safe Browsing API key is not set" }
    println("Checking URL safety for: $input")

    val endpoint = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$googleSafeBrowsingApiKey"
      // Set up the URL connection
      val url = URL(endpoint)
      println("URL: $url")
      println("API Key: $googleSafeBrowsingApiKey")
      val openedConnection = url.openConnection() as HttpURLConnection
      openedConnection.requestMethod = "POST"
      openedConnection.doOutput = true
      openedConnection.setRequestProperty("Content-Type", "application/json")
      // JSON body for the POST request
      val requestBody = """
      {
        "client": {
          "clientId": "url-shortener",
          "clientVersion": "1.5.2"
        },
        "threatInfo": {
          "threatTypes": ["MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"],
          "platformTypes": ["ANY_PLATFORM"],
          "threatEntryTypes": ["URL"],
          "threatEntries": [
            {"url": "$input"}
          ]
        }
      }
      """.trimIndent()

      // Send the request
      openedConnection.outputStream.use { os ->
          val inputBytes = requestBody.toByteArray()
          os.write(inputBytes, 0, inputBytes.size)
      }

      // Get the response code
      val responseCode = openedConnection.responseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
          // Read the response and parse it as JSON
          val response = openedConnection.inputStream.bufferedReader().use { it.readText() }
          println("Response: $response")
          // Parse the response JSON
          val jsonResponse = JSONObject(response)
          // Check if the "matches" array is present and has elements
          val matches = jsonResponse.optJSONArray("matches")
          return !(matches != null && matches.length() > 0)
      } else {
          return false
      }

    }
}

