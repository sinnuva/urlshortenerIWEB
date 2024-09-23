package es.unizar.urlshortener.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository

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
interface ClickEntityRepository : JpaRepository<ClickEntity, Long>
