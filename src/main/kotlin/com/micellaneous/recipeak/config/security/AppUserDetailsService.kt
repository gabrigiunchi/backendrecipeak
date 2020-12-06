package com.micellaneous.recipeak.config.security

import com.micellaneous.recipeak.dao.UserDAO
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class AppUserDetailsService(private val users: UserDAO) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        return this.users.findByUsername(username)
            .let { AppUserDetails(it ?: throw UsernameNotFoundException("Username: $username not found")) }
    }
}