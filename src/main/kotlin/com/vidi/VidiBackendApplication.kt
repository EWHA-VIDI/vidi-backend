package com.vidi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VidiBackendApplication

fun main(args: Array<String>) {
    runApplication<VidiBackendApplication>(*args)
}
