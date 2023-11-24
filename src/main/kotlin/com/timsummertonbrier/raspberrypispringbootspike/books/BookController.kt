package com.timsummertonbrier.raspberrypispringbootspike.books

import com.timsummertonbrier.raspberrypispringbootspike.authors.Author
import com.timsummertonbrier.raspberrypispringbootspike.authors.AuthorRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class Book(
    val id: Int,
    val title: String,
    val category: String,
    val author: Author,
)

data class BookRequest(
    @field:NotBlank
    val title: String? = null,

    @field:NotBlank
    val category: String? = null,

    @field:Positive
    val authorId: Int? = null
) {
    companion object {
        fun fromBook(book: Book): BookRequest {
            return BookRequest(
                book.title,
                book.category,
                book.author.id,
            )
        }
    }
}

@Controller
@RequestMapping("/books")
class BookController(private val bookRepository: BookRepository, private val authorRepository: AuthorRepository) {

    @GetMapping
    fun books(model: Model): String {
        model.addAttribute("books", bookRepository.getAllBooks())
        return "books/view-all"
    }

    @GetMapping("/add")
    fun showAddBookForm(model: Model): String {
        model
            .addAttribute("bookRequest", BookRequest())
            .addAttribute("authors", authorRepository.getAllAuthors())
        return "books/add"
    }

    @GetMapping("/edit/{id}")
    fun showUpdateBookForm(model: Model, @PathVariable("id") id: Int): String {
        model
            .addAttribute("id", id)
            .addAttribute("bookRequest", BookRequest.fromBook(bookRepository.getBook(id)))
            .addAttribute("authors", authorRepository.getAllAuthors())
        return "books/edit"
    }

    @PostMapping("/add")
    fun addBook(@Valid bookRequest: BookRequest, bindingResult: BindingResult, model: Model): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorRepository.getAllAuthors())
            return "books/add"
        }

        bookRepository.addBook(bookRequest)
        return "redirect:/books"
    }

    @PostMapping("/update/{id}")
    fun updateBook(@PathVariable("id") id: Int, @Valid bookRequest: BookRequest, bindingResult: BindingResult, model: Model): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorRepository.getAllAuthors())
            return "books/edit"
        }

        bookRepository.updateBook(id, bookRequest)
        return "redirect:/books"
    }

    @DeleteMapping("/delete/{id}")
    fun deleteBook(@PathVariable("id") id: Int): String {
        bookRepository.deleteBook(id)
        return "redirect:/books"
    }
}