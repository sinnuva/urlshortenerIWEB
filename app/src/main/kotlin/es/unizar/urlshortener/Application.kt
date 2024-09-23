package es.unizar.urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The marker that makes this project a Spring Boot application.
 */
@SpringBootApplication
class Application

/**
 * The main entry point.
 * @param args The command line arguments.
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<Application>(*args)
}
