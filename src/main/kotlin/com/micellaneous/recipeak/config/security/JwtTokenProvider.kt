package com.micellaneous.recipeak.config.security


import com.micellaneous.recipeak.model.AppUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
class JwtTokenProvider(private val userDetailsService: AppUserDetailsService) {

    @Value("\${security.jwt.token.secret-key:secret}")
    private var secretKey = "secret"

    @Value("\${security.jwt.token.expireHours}")
    private val validityInHours: Long = 0

    fun createToken(user: AppUser) = this.createToken(user.username, listOf(user.type.name))

    fun createToken(username: String, roles: List<String>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles
        val now = Date()
        val validity = Date(now.time + validityInHours * 60 * 60 * 1000)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUsername(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun getUsername(token: String): String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
    }

    fun resolveToken(req: HttpServletRequest): String? {
        val bearerToken: String? = req.getHeader("Authorization")
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else null
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims: Jws<Claims> = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            claims.body.expiration > Date()
        } catch (e: Exception) {
            false
        }
    }
}