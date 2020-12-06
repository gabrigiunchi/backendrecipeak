package com.micellaneous.recipeak.model.dto.input

import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.enum.UserType

class UserDTOInput(
    val username: String,
    val password: String,
    val name: String,
    val surname: String,
    val email: String,
    val type: UserType,
    val isActive: Boolean
) {

    constructor(username: String, password: String, name: String, surname: String, email: String, type: UserType) :
            this(username, password, name, surname, email, type, true)

    constructor(user: AppUser) :
            this(
                user.username, user.password, user.name, user.surname, user.email,
                user.type, user.active
            )
}