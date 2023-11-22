package com.timsummertonbrier.raspberrypispringbootspike

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class Book(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val category: String,
)
data class NewBook(val title: String? = null, val category: String? = null)

@Repository
class BookRepository {

    private val books = mutableListOf<Book>()

    fun getAllBooks() = books

    fun addBook(book: Book) = books.add(book)
}

@Controller
@RequestMapping("/books")
class BookController(private val bookRepository: BookRepository) {

    @GetMapping
    fun books(model: Model): String {
        model.addAttribute("books", bookRepository.getAllBooks())
        return "books"
    }

    @GetMapping("/add")
    fun showAddBookForm(model: Model): String {
        model.addAttribute("book", NewBook())
        return "add-book"
    }

    @PostMapping("/add")
    fun addBook(@Valid book: Book, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "add-book"
        }

        bookRepository.addBook(book)
        return "redirect:/books"
    }
}