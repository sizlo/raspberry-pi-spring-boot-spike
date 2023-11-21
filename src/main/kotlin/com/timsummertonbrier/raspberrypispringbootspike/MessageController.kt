package com.timsummertonbrier.raspberrypispringbootspike

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MessageController {

    @GetMapping("message")
    fun message(model: Model): String {
        val messages = listOf(
            "A wizard is never late, nor is he early, he arrives precisely when he means to",
            "When in doubt, always follow your nose",
            "Fool of a Took!",
        )

        model.addAttribute("message", messages.random())
        return "message"
    }
}