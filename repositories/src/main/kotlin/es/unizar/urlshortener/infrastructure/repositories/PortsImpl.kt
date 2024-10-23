package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import java.time.OffsetDateTime

/**
 * Implementation of the port [ClickRepositoryService].
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    /**
     * Saves a [Click] entity to the repository.
     *
     * @param cl The [Click] entity to be saved.
     * @return The saved [Click] entity.
     */
    override fun save(cl: Click): Click = clickEntityRepository.save(cl.toEntity()).toDomain()

    /**
     * Counts the number of clicks (redirections) for a specific hash within a given time range.
     *
     * @param hash The hash of the URL to count clicks for.
     * @param currentTime The current time.
     * @param startTime The start time of the period in which to count clicks.
     * @return The number of clicks in the specified time range.
     */
    override fun countClicksInTimeRange(hash: String, currentTime: OffsetDateTime, startTime: OffsetDateTime): Long {
        return clickEntityRepository.countClicksInTimeRange(hash, currentTime, startTime)
    }
}

/**
 * Implementation of the port [ShortUrlRepositoryService].
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    /**
     * Finds a [ShortUrl] entity by its key.
     *
     * @param id The key of the [ShortUrl] entity.
     * @return The found [ShortUrl] entity or null if not found.
     */
    override fun findByKey(id: String): ShortUrl? = shortUrlEntityRepository.findByHash(id)?.toDomain()

    /**
     * Saves a [ShortUrl] entity to the repository.
     *
     * @param su The [ShortUrl] entity to be saved.
     * @return The saved [ShortUrl] entity.
     */
    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()
}
