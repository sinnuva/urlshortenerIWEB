package es.unizar.urlshortener.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

/**
 * Specification of the repository of [ShortUrlEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ShortUrlEntityRepository : JpaRepository<ShortUrlEntity, String> {
    /**
     * Finds a [ShortUrlEntity] by its hash.
     *
     * @param hash The hash of the [ShortUrlEntity].
     * @return The found [ShortUrlEntity] or null if not found.
     */
    fun findByHash(hash: String): ShortUrlEntity?
}

/**
 * Specification of the repository of [ClickEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ClickEntityRepository : JpaRepository<ClickEntity, Long> {
    /**
     * Counts the number of clicks (redirections) for a given URL (identified by its hash)
     * that occurred between the provided start time and the current time.
     *
     * @param hash The hash of the URL to count clicks for.
     * @param currentTime The current time (end of the time range).
     * @param startTime The start time of the period in which to count clicks.
     * @return The number of clicks in the specified time range.
     */
    @Query("SELECT COUNT(c) FROM ClickEntity c WHERE c.hash = :hash AND c.created BETWEEN :startTime AND :currentTime")
    fun countClicksInTimeRange(
        @Param("hash") hash: String,
        @Param("currentTime") currentTime: OffsetDateTime,
        @Param("startTime") startTime: OffsetDateTime
    ): Long
}
