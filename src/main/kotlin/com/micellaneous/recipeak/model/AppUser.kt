package com.micellaneous.recipeak.model

import com.micellaneous.recipeak.model.enum.UserType
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int,

    @Column(unique = true, nullable = false)
    val username: String,
    var password: String,
    var name: String,
    var surname: String,
    var email: String,

    @Enumerated
    var type: UserType,

    val validFrom: OffsetDateTime,
    val expireDate: OffsetDateTime
) {

    var active = true

    constructor(username: String, password: String, name: String, surname: String, email: String, type: UserType) :
            this(
                -1, username, password, name, surname, email, type, OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(1000)
            )


    constructor(username: String, password: String, name: String, surname: String, email: String) :
            this(username, password, name, surname, email, UserType.USER)


    override fun toString(): String {
        return "{id:$id, username:$username, password:$password, name:$name, surname:$surname, email:$email}"
    }
}