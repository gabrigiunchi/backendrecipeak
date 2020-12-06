package com.micellaneous.recipeak.config.security

import com.micellaneous.recipeak.constants.ApiUrls
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : WebSecurityConfigurerAdapter() {

    private val whitelist = arrayOf(
        "/index",
        "/swagger-ui.html",
        "/swagger-ui.html/**",
        "/swagger-resources/**",
        "/v2/api-docs",
        "/webjars/**",
        "_ah/**",
        ApiUrls.LOGIN,
        ApiUrls.LOGIN + "/token",
        ApiUrls.ALIVE
    )

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .httpBasic().disable()
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(*whitelist).permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(JwtTokenFilter(this.jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authProvider())
    }

    @Bean
    fun authProvider(): DaoAuthenticationProvider? {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(BCryptPasswordEncoder())
        return authProvider
    }
}