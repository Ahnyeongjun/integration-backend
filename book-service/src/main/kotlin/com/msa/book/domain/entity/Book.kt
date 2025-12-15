package com.msa.book.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "books")
class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(length = 20)
    var isbn: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,

    @Column(name = "published_date")
    var publishedDate: LocalDate? = null,

    @Column
    var pages: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    var author: Author? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    var publisher: Publisher? = null,

    @ManyToMany
    @JoinTable(
        name = "book_tags",
        joinColumns = [JoinColumn(name = "book_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    val tags: MutableSet<Tag> = mutableSetOf(),

    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,

    @Column(name = "review_count")
    var reviewCount: Int = 0
) : BaseTimeEntity()

@Entity
@Table(name = "authors")
class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(columnDefinition = "TEXT")
    var bio: String? = null
) : BaseTimeEntity()

@Entity
@Table(name = "publishers")
class Publisher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 100)
    var name: String
) : BaseTimeEntity()

@Entity
@Table(name = "tags")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true, length = 50)
    var name: String
)
