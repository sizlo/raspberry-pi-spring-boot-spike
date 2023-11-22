package com.timsummertonbrier.raspberrypispringbootspike

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
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
)

object Books : IntIdTable("book") {
    var title = text("title")
    var category = text("category")
    var authorId = reference("author_id", Authors)
}

@Repository
@Transactional
class BookRepository {
    fun getAllBooks(): List<Book> {
        return (Books innerJoin Authors).selectAll().map {
            Book(
                it[Books.id].value,
                it[Books.title],
                it[Books.category],
                Author(
                    it[Authors.id].value,
                    it[Authors.firstName],
                    it[Authors.lastName]
                )
            )
        }
    }

    fun addBook(book: BookRequest) {
        Books.insert {
            it[title] = book.title!!
            it[category] = book.category!!
            it[authorId] = book.authorId!!
        }
    }
}

@Controller
@RequestMapping("/books")
class BookController(private val bookRepository: BookRepository, private val authorRepository: AuthorRepository) {

    @GetMapping
    fun books(model: Model): String {
        model.addAttribute("books", bookRepository.getAllBooks())
        return "books"
    }

    @GetMapping("/add")
    fun showAddBookForm(model: Model): String {
        model
            .addAttribute("book", BookRequest())
            .addAttribute("authors", authorRepository.getAllAuthors())
        return "add-book"
    }

    @PostMapping("/add")
    fun addBook(@Valid book: BookRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "add-book"
        }

        bookRepository.addBook(book)
        return "redirect:/books"
    }
}