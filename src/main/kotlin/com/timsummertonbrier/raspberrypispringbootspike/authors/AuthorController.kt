package com.timsummertonbrier.raspberrypispringbootspike.authors

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class Author(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    val name = "$firstName $lastName"
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

@Controller
@RequestMapping("/authors")
class AuthorController(private val authorRepository: AuthorRepository) {

    @GetMapping
    fun authors(model: Model): String {
        model.addAttribute("authors", authorRepository.getAllAuthors())
        return "authors/view-all"
    }

    @GetMapping("/add")
    fun showAddAuthorForm(model: Model): String {
        model.addAttribute("authorRequest", AuthorRequest())
        return "authors/add"
    }

    @GetMapping("/edit/{id}")
    fun showUpdateAuthorForm(model: Model, @PathVariable("id") id: Int): String {
        model
            .addAttribute("id", id)
            .addAttribute("authorRequest", AuthorRequest.fromAuthor(authorRepository.getAuthor(id)))
        return "authors/edit"
    }

    @PostMapping("/add")
    fun addAuthor(@Valid authorRequest: AuthorRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "authors/add"
        }

        authorRepository.addAuthor(authorRequest)
        return "redirect:/authors"
    }

    @PostMapping("/update/{id}")
    fun updateBook(@PathVariable("id") id: Int, @Valid authorRequest: AuthorRequest, bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "authors/edit"
        }

        authorRepository.updateAuthor(id, authorRequest)
        return "redirect:/authors"
    }
}