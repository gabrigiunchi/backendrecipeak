package com.micellaneous.recipeak.model.dto.output

import com.micellaneous.recipeak.model.AppUser

data class UserDTOOutput(
    val id: Int,
    val username: String,
    val name: String,
    val surname: String,
    val email: String
) {

    constructor(user: AppUser) : this(user.id, user.username, user.name, user.surname, user.email)
}