package com.msa.ticketing.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "concerts")
class Concert(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "artist_name", length = 100)
    var artistName: String? = null,

    @Column(nullable = false, length = 500)
    var venue: String,

    @Column(name = "concert_date", nullable = false)
    var concertDate: LocalDateTime,

    @Column(name = "ticket_open_date")
    var ticketOpenDate: LocalDateTime? = null,

    @Column(name = "total_seats")
    var totalSeats: Int = 0,

    @Column(name = "available_seats")
    var availableSeats: Int = 0,

    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ConcertStatus = ConcertStatus.UPCOMING,

    @OneToMany(mappedBy = "concert", cascade = [CascadeType.ALL])
    val seats: MutableList<Seat> = mutableListOf()

) : BaseTimeEntity()

enum class ConcertStatus {
    UPCOMING, ON_SALE, SOLD_OUT, COMPLETED, CANCELLED
}

@Entity
@Table(name = "seats")
class Seat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    val concert: Concert,

    @Column(name = "seat_number", nullable = false, length = 20)
    val seatNumber: String,

    @Column(name = "seat_grade", length = 20)
    var seatGrade: String? = null,

    @Column(nullable = false)
    var price: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SeatStatus = SeatStatus.AVAILABLE

) : BaseTimeEntity()

enum class SeatStatus {
    AVAILABLE, RESERVED, SOLD
}

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    val concert: Concert,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    val seat: Seat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null

) : BaseTimeEntity()

enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, EXPIRED
}
