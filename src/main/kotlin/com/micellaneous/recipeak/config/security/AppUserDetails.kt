package com.micellaneous.recipeak.config.security

import com.micellaneous.recipeak.model.AppUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.OffsetDateTime

class AppUserDetails(val user: AppUser) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return listOf(SimpleGrantedAuthority(user.type.name))
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isAccountNonExpired(): Boolean {
        return OffsetDateTime.now() in user.validFrom..user.expireDate
    }

    override fun isAccountNonLocked(): Boolean {
        return user.active
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return user.active
    }
}