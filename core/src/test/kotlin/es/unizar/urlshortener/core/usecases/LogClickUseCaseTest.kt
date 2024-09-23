package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ClickRepositoryService
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

class LogClickUseCaseTest {

    @Test
    fun `logClick fails silently`() {
        val repository = mock<ClickRepositoryService> ()
        val clickProperties = mock<ClickProperties>()
        whenever(repository.save(any())).thenThrow(RuntimeException())

        val useCase = LogClickUseCaseImpl(repository)

        useCase.logClick("key", clickProperties)
        verify(repository).save(any())
    }
}

