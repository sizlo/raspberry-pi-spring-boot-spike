package com.timsummertonbrier.raspberrypispringbootspike

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalModelAttributes(
    @Value("\${app.version}") private val appVersion: String,
    @Value("\${app.environment}") private val appEnvironment: String,
    @Value("\${spring.datasource.url}") private val jdbcUrl: String,
) {
    @ModelAttribute("buildInfo")
    fun buildInfo(): String {
        val databaseEnvironment = when {
            jdbcUrl.contains("localhost") -> "local"
            jdbcUrl.contains("_dev") -> "dev"
            else -> "prod"
        }
        return "$appVersion - running on: $appEnvironment - database: $databaseEnvironment"
    }
}