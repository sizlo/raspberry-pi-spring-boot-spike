package com.timsummertonbrier.raspberrypispringbootspike

import com.timsummertonbrier.raspberrypispringbootspike.categories.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class HomeController(private val categoryRepository: CategoryRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun home(model: Model): String {
        logger.info("Showing home page")
        model.addAttribute("categories", categoryRepository.getAllCategories())
        return "home"
    }
}