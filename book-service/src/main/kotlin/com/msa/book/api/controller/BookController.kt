package com.msa.book.api.controller

import com.msa.book.domain.entity.Book
import com.msa.book.domain.repository.BookRepository
import com.msa.common.exception.NotFoundException
import com.msa.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookRepository: BookRepository
) {
    @Operation(summary = "도서 목록")
    @GetMapping
    fun getBooks(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<BookResponse>> {
        return ApiResponse.success(bookRepository.findAll(pageable).map { BookResponse.from(it) })
    }

    @Operation(summary = "도서 상세")
    @GetMapping("/{id}")
    fun getBook(@PathVariable id: Long): ApiResponse<BookResponse> {
        val book = bookRepository.findById(id).orElseThrow { NotFoundException("Book", id) }
        return ApiResponse.success(BookResponse.from(book))
    }

    @Operation(summary = "도서 검색")
    @GetMapping("/search")
    fun searchBooks(
        @RequestParam keyword: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<BookResponse>> {
        return ApiResponse.success(bookRepository.searchByKeyword(keyword, pageable).map { BookResponse.from(it) })
    }

    @Operation(summary = "인기 도서")
    @GetMapping("/popular")
    fun getPopularBooks(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<BookResponse>> {
        return ApiResponse.success(bookRepository.findPopular(pageable).map { BookResponse.from(it) })
    }
}

data class BookResponse(
    val id: Long,
    val title: String,
    val isbn: String?,
    val description: String?,
    val coverImage: String?,
    val authorName: String?,
    val publisherName: String?,
    val avgRating: Double,
    val reviewCount: Int
) {
    companion object {
        fun from(book: Book) = BookResponse(
            id = book.id,
            title = book.title,
            isbn = book.isbn,
            description = book.description,
            coverImage = book.coverImage,
            authorName = book.author?.name,
            publisherName = book.publisher?.name,
            avgRating = book.avgRating,
            reviewCount = book.reviewCount
        )
    }
}
