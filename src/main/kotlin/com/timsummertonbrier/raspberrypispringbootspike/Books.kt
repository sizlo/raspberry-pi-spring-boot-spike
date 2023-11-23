package com.timsummertonbrier.raspberrypispringbootspike

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class Book(
    val id: Int,
    val title: String,
    val category: String,
    val author: Author,
) {
    companion object {
        fun fromRow(row: ResultRow): Book {
            return Book(
                row[Books.id].value,
                row[Books.title],
                row[Books.category],
                Author(
                    row[Authors.id].value,
                    row[Authors.firstName],
                    row[Authors.lastName]
                )
            )
        }
    }
}

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

object Books : IntIdTable("book") {
    var title = text("title")
    var category = text("category")
    var authorId = reference("author_id", Authors)

    fun UpdateBuilder<Int>.populateFrom(bookRequest: BookRequest) {
        this[title] = bookRequest.title!!
        this[category] = bookRequest.category!!
        this[authorId] = bookRequest.authorId!!
    }
}

@Repository
@Transactional
class BookRepository {
    fun getAllBooks(): List<Book> {
        return (Books innerJoin Authors).selectAll().map { Book.fromRow(it) }
    }

    fun getBook(id: Int): Book {
        return (Books innerJoin Authors).select({ Books.id eq id }).map { Book.fromRow(it) }.first()
    }

    fun addBook(bookRequest: BookRequest) {
        Books.insert { it.populateFrom(bookRequest) }
    }

    fun updateBook(id: Int, bookRequest: BookRequest) {
        Books.update({ Books.id eq id }) { it.populateFrom(bookRequest) }
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
            .addAttribute("bookRequest", BookRequest())
            .addAttribute("authors", authorRepository.getAllAuthors())
        return "add-book"
    }

    @GetMapping("/edit/{id}")
    fun showUpdateBookForm(model: Model, @PathVariable("id") id: Int): String {
        model
            .addAttribute("id", id)
            .addAttribute("bookRequest", BookRequest.fromBook(bookRepository.getBook(id)))
            .addAttribute("authors", authorRepository.getAllAuthors())
        return "edit-book"
    }

    @PostMapping("/add")
    fun addBook(@Valid bookRequest: BookRequest, bindingResult: BindingResult, model: Model): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorRepository.getAllAuthors())
            return "add-book"
        }

        bookRepository.addBook(bookRequest)
        return "redirect:/books"
    }

    @PostMapping("/update/{id}")
    fun updateBook(@PathVariable("id") id: Int, @Valid bookRequest: BookRequest, bindingResult: BindingResult, model: Model): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorRepository.getAllAuthors())
            return "edit-book"
        }

        bookRepository.updateBook(id, bookRequest)
        return "redirect:/books"
    }
}