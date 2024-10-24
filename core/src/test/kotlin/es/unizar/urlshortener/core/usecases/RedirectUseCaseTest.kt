package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.TooManyRequestsException
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.MAX_REDIRECTIONS
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RedirectUseCaseTest {

    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService> ()
        val clickRepositoryService = mock<ClickRepositoryService>()
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl("key", redirection)
        whenever(shortUrlRepository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(shortUrlRepository, clickRepositoryService)

        assertEquals(redirection, useCase.redirectTo("key"))
    }

    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService> ()
        val clickRepositoryService = mock<ClickRepositoryService>()
        whenever(shortUrlRepository.findByKey("key")).thenReturn(null)
        val useCase = RedirectUseCaseImpl(shortUrlRepository, clickRepositoryService)

        assertFailsWith<RedirectionNotFound> {
            useCase.redirectTo("key")
        }
    }

    @Test
    fun `redirectTo returns a not found when find by key fails`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService> ()
        val clickRepositoryService = mock<ClickRepositoryService>()
        whenever(shortUrlRepository.findByKey("key")).thenThrow(RuntimeException())
        val useCase = RedirectUseCaseImpl(shortUrlRepository, clickRepositoryService)

        assertFailsWith<InternalError> {
            useCase.redirectTo("key")
        }
    }

    @Test
    fun `redirectTo returns too many requests exception when redirection limit is reached`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val clickRepositoryService = mock<ClickRepositoryService>()
        whenever(
            clickRepositoryService.countClicksInTimeRange(
                eq("key"),
                any(),
                any()
            )
        ).thenReturn(MAX_REDIRECTIONS.toLong())
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl("key", redirection)
        whenever(shortUrlRepository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(shortUrlRepository, clickRepositoryService)

        assertFailsWith<TooManyRequestsException> {
            useCase.redirectTo("key")
        }
    }
}
