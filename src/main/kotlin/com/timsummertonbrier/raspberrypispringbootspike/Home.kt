package com.timsummertonbrier.raspberrypispringbootspike

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class HomeController(private val categoryRepository: CategoryRepository) {
    @GetMapping
    fun home(model: Model): String {
        model.addAttribute("categories", categoryRepository.getAllCategories())
        return "home"
    }
}