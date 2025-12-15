package com.msa.book.domain.repository

import com.msa.book.domain.entity.Author
import com.msa.book.domain.entity.Book
import com.msa.book.domain.entity.Publisher
import com.msa.book.domain.entity.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookRepository : JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author.name LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Book>
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<Book>
    @Query("SELECT b FROM Book b ORDER BY b.avgRating DESC")
    fun findPopular(pageable: Pageable): Page<Book>
}
interface AuthorRepository : JpaRepository<Author, Long>
interface PublisherRepository : JpaRepository<Publisher, Long>
interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(name: String): Tag?
}
