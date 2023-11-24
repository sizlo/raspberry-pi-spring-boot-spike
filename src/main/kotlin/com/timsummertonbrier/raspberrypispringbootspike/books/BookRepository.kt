package com.timsummertonbrier.raspberrypispringbootspike.books

import com.timsummertonbrier.raspberrypispringbootspike.authors.Author
import com.timsummertonbrier.raspberrypispringbootspike.authors.Authors
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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

fun ResultRow.toBook(): Book {
    return Book(
        this[Books.id].value,
        this[Books.title],
        this[Books.category],
        Author(
            this[Authors.id].value,
            this[Authors.firstName],
            this[Authors.lastName]
        )
    )
}

@Repository
@Transactional
class BookRepository {
    fun getAllBooks(): List<Book> {
        return (Books innerJoin Authors).selectAll().map { it.toBook() }
    }

    fun getBook(id: Int): Book {
        return (Books innerJoin Authors).select { Books.id eq id }.map { it.toBook() }.first()
    }

    fun addBook(bookRequest: BookRequest) {
        Books.insert { it.populateFrom(bookRequest) }
    }

    fun updateBook(id: Int, bookRequest: BookRequest) {
        Books.update({ Books.id eq id }) { it.populateFrom(bookRequest) }
    }
}