package com.timsummertonbrier.raspberrypispringbootspike.authors

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

object Authors : IntIdTable("author") {
    var firstName = text("firstName")
    var lastName = text("lastName")

    fun UpdateBuilder<Int>.populateFrom(authorRequest: AuthorRequest) {
        this[firstName] = authorRequest.firstName!!
        this[lastName] = authorRequest.lastName!!
    }
}

fun ResultRow.toAuthor(): Author {
    return Author(
        this[Authors.id].value,
        this[Authors.firstName],
        this[Authors.lastName]
    )
}

@Repository
@Transactional
class AuthorRepository {
    fun getAllAuthors(): List<Author> {
        return Authors.selectAll().map { it.toAuthor() }
    }

    fun getAuthor(id: Int): Author {
        return Authors.select { Authors.id eq id }.map { it.toAuthor() }.first()
    }

    fun addAuthor(authorRequest: AuthorRequest) {
        Authors.insert { it.populateFrom(authorRequest) }
    }

    fun updateAuthor(id: Int, authorRequest: AuthorRequest) {
        Authors.update({ Authors.id eq id }) { it.populateFrom(authorRequest) }
    }
}