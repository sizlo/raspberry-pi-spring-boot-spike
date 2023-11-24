package com.timsummertonbrier.raspberrypispringbootspike.categories

import com.timsummertonbrier.raspberrypispringbootspike.authors.Authors
import com.timsummertonbrier.raspberrypispringbootspike.books.Book
import com.timsummertonbrier.raspberrypispringbootspike.books.Books
import com.timsummertonbrier.raspberrypispringbootspike.books.toBook
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Repository
@Transactional
class CategoryRepository {
    fun getAllCategories(): List<String> {
        return Books.slice(Books.category).selectAll().withDistinct().map { it[Books.category] }
    }

    fun getBooksInCategory(category: String): List<Book> {
        return (Books innerJoin Authors).select { Books.category eq category }.map { it.toBook() }
    }
}

@Controller
@RequestMapping("/categories")
class CategoryController(private val categoryRepository: CategoryRepository) {
    @GetMapping("/{category}")
    fun category(@PathVariable("category") category: String, model: Model): String {
        model
            .addAttribute("category", category)
            .addAttribute("books", categoryRepository.getBooksInCategory(category))
        return "categories/view-one"
    }
}