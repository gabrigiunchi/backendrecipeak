package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.dao.UserDAO
import com.micellaneous.recipeak.exception.AccessDeniedException
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.enum.UserType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

open class BaseController(private val userDAO: UserDAO) {

    protected fun getLoggedUser(): AppUser {
        return this.userDAO.findByUsername((SecurityContextHolder.getContext().authentication.principal as UserDetails).username)!!
    }

    protected fun isAdmin(): Boolean {
        return this.isAdmin(this.getLoggedUser())
    }

    protected fun isAdmin(user: AppUser): Boolean {
        return user.type == UserType.ADMINISTRATOR
    }

    private fun canAccessUserInfo(requestUserId: Int): Boolean {
        val loggedUser = this.getLoggedUser()
        return this.isAdmin(loggedUser) || loggedUser.id == requestUserId
    }

    protected fun applyUserInfoProtection(requestUserId: Int) {
        if (!this.canAccessUserInfo(requestUserId)) {
            throw AccessDeniedException("You don't have the rights to access user $requestUserId information")
        }
    }
}