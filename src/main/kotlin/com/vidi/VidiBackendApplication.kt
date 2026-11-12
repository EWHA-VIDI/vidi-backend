package com.vidi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VidiBackendApplication

@Suppress("SpreadOperator") // Spring Boot requires a vararg entry point.
fun main(args: Array<String>) {
    runApplication<VidiBackendApplication>(*args)
}
