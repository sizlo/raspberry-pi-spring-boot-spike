package com.timsummertonbrier.raspberrypispringbootspike

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class Author(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    val name = "$firstName $lastName"

    companion object {
        fun fromRow(row: ResultRow): Author {
            return Author(
                row[Authors.id].value,
                row[Authors.firstName],
                row[Authors.lastName]
            )
        }
    }
}

data class AuthorRequest(
    @field:NotBlank
    val firstName: String? = null,

    @field:NotBlank
    val lastName: String? = null,
) {
    companion object {
        fun fromAuthor(author: Author): AuthorRequest {
            return AuthorRequest(
                author.firstName,
                author.lastName
            )
        }
    }
}

object Authors : IntIdTable("author") {
    var firstName = text("firstName")
    var lastName = text("lastName")

    fun UpdateBuilder<Int>.populateFrom(authorRequest: AuthorRequest) {
        this[firstName] = authorRequest.firstName!!
        this[lastName] = authorRequest.lastName!!
    }
}

@Repository
@Transactional
class AuthorRepository {
    fun getAllAuthors(): List<Author> {
        return Authors.selectAll().map { Author.fromRow(it) }
    }

    fun getAuthor(id: Int): Author {
        return Authors.select({ Authors.id eq id }).map { Author.fromRow(it) }.first()
    }

    fun addAuthor(authorRequest: AuthorRequest) {
        Authors.insert { it.populateFrom(authorRequest) }
    }

    fun updateAuthor(id: Int, authorRequest: AuthorRequest) {
        Authors.update({ Authors.id eq id }) { it.populateFrom(authorRequest) }
    }
}

@Controller
@RequestMapping("/authors")
class AuthorController(private val authorRepository: AuthorRepository) {

    @GetMapping
    fun authors(model: Model): String {
        model.addAttribute("authors", authorRepository.getAllAuthors())
        return "authors"
    }

    @GetMapping("/add")
    fun showAddAuthorForm(model: Model): String {
        model.addAttribute("authorRequest", AuthorRequest())
        return "add-author"
    }

    @GetMapping("/edit/{id}")
    fun showUpdateAuthorForm(model: Model, @PathVariable("id") id: Int): String {
        model
            .addAttribute("id", id)
            .addAttribute("authorRequest", AuthorRequest.fromAuthor(authorRepository.getAuthor(id)))
        return "edit-author"
    }

    @PostMapping("/add")
    fun addAuthor(@Valid authorRequest: AuthorRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "add-author"
        }

        authorRepository.addAuthor(authorRequest)
        return "redirect:/authors"
    }

    @PostMapping("/update/{id}")
    fun updateBook(@PathVariable("id") id: Int, @Valid authorRequest: AuthorRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "edit-author"
        }

        authorRepository.updateAuthor(id, authorRequest)
        return "redirect:/authors"
    }
}