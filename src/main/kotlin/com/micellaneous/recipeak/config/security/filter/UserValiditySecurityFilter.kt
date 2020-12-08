package com.micellaneous.recipeak.config.security.filter

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class UserValiditySecurityFilter : GenericFilterBean() {

    override fun doFilter(req: ServletRequest?, res: ServletResponse?, chain: FilterChain) {
        val auth = SecurityContextHolder.getContext().authentication
        SecurityContextHolder.getContext().authentication = if (isActive(auth)) auth else null
        chain.doFilter(req, res)
    }

    private fun isActive(authentication: Authentication?): Boolean {
        return authentication?.let {
            val principal = authentication.principal as UserDetails
            principal.isEnabled &&
                    principal.isAccountNonExpired &&
                    principal.isAccountNonLocked &&
                    principal.isCredentialsNonExpired
        } ?: false
    }
}