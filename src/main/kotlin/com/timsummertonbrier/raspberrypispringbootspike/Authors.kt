package com.timsummertonbrier.raspberrypispringbootspike

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
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
)

object Authors : IntIdTable("author") {
    var firstName = text("firstName")
    var lastName = text("lastName")
}

@Repository
@Transactional
class AuthorRepository {
    fun getAllAuthors(): List<Author> {
        return Authors.selectAll().map { Author.fromRow(it) }
    }

    fun addAuthor(author: AuthorRequest) {
        Authors.insert {
            it[firstName] = author.firstName!!
            it[lastName] = author.lastName!!
        }
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
        model.addAttribute("author", AuthorRequest())
        return "add-author"
    }

    @PostMapping("/add")
    fun addAuthor(@Valid author: AuthorRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "add-author"
        }

        authorRepository.addAuthor(author)
        return "redirect:/authors"
    }
}