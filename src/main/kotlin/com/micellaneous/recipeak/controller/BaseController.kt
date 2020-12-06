package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.UserDAO
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.enum.UserType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

open class BaseController(private val userDAO: UserDAO) {

    protected fun getLoggedUser(): AppUser {
        return this.userDAO.findByUsername((SecurityContextHolder.getContext().authentication.principal as UserDetails).username)!!
    }


    protected fun isAdmin(): Boolean {
        return this.getLoggedUser().type == UserType.ADMINISTRATOR
    }
}