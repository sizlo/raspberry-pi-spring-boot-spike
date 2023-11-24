package com.timsummertonbrier.raspberrypispringbootspike

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalModelAttributes(@Value("\${app.version}") private val appVersion: String) {
    @ModelAttribute("version")
    fun version() = appVersion
}