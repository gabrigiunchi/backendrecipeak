package com.micellaneous.recipeak.config.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class JwtTokenFilter(private val jwtTokenProvider: JwtTokenProvider) : GenericFilterBean() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest?, res: ServletResponse?, filterChain: FilterChain) {
        val token: String? = jwtTokenProvider.resolveToken(req as HttpServletRequest)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            SecurityContextHolder.getContext().authentication = jwtTokenProvider.getAuthentication(token)
        }
        filterChain.doFilter(req, res)
    }
}