@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.repositories

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * The [ClickEntity] entity logs clicks.
 */
@Entity
@Table(name = "click")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class ClickEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long?,
    val hash: String,
    val created: OffsetDateTime,
    val ip: String?,
    val referrer: String?,
    val browser: String?,
    val platform: String?,
    val country: String?
)

/**
 * The [ShortUrlEntity] entity stores short urls.
 */
@Entity
@Table(name = "shorturl")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class ShortUrlEntity(
    @Id
    val hash: String,
    val target: String,
    val sponsor: String?,
    val created: OffsetDateTime,
    val owner: String?,
    val mode: Int,
    val safe: Boolean,
    val ip: String?,
    val country: String?,
    var validationStatus: ValidationStatus = ValidationStatus.PENDING
)

/**
 * Enum class that represents the validation status of a [ShortUrl].
 */
enum class ValidationStatus {
    PENDING,
    REACHABLE,
    UNREACHABLE
}

/**
 * The [ValidationStatus] enum represents the validation status of a short URL.
 */
data class ShortUrl(
    val hash: String,
    val target: String,
    var validationStatus: ValidationStatus // Enum en el dominio
)

