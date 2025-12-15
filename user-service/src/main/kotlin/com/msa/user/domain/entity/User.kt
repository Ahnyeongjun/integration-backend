package com.msa.user.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    val id: Long,  // Auth service에서 생성된 ID 사용

    @Column(unique = true, length = 100)
    var email: String? = null,

    @Column(length = 50)
    var nickname: String? = null,

    @Column(name = "profile_image", length = 500)
    var profileImage: String? = null,

    @Column(length = 200)
    var bio: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE

) : BaseTimeEntity() {

    fun updateProfile(nickname: String?, profileImage: String?, bio: String?) {
        nickname?.let { this.nickname = it }
        profileImage?.let { this.profileImage = it }
        bio?.let { this.bio = it }
    }

    fun deactivate() {
        this.status = UserStatus.INACTIVE
    }

    fun delete() {
        this.status = UserStatus.DELETED
        this.email = null
        this.nickname = "탈퇴한 사용자"
        this.profileImage = null
        this.bio = null
    }
}

enum class UserStatus {
    ACTIVE, INACTIVE, DELETED
}
