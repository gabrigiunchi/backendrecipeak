package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.config.security.service.JwtTokenProvider
import com.micellaneous.recipeak.model.dto.input.ValidateTokenRequest
import com.micellaneous.recipeak.model.dto.input.ValidateUserDTO
import com.micellaneous.recipeak.model.dto.output.Token
import com.micellaneous.recipeak.model.dto.output.TokenLoginResponse
import com.micellaneous.recipeak.model.dto.output.UserDTOOutput
import com.micellaneous.recipeak.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/login")
class LoginController(private val userService: UserService, val jwtTokenProvider: JwtTokenProvider) {

    private val logger = LoggerFactory.getLogger(LoginController::class.java)


    @PostMapping
    fun login(@RequestBody @Validated credentials: ValidateUserDTO): ResponseEntity<Token> {
        this.logger.info("Login request: {username:" + credentials.username + ", password:" + credentials.password + "}")
        val user = this.userService.authenticate(credentials)
        val token = this.jwtTokenProvider.createToken(user.username, listOf(user.type.name))
        return ResponseEntity.ok(Token(UserDTOOutput(user), token))
    }

    @PostMapping("/token")
    fun loginWithToken(@RequestBody request: ValidateTokenRequest): ResponseEntity<TokenLoginResponse> {
        this.logger.info("Login with token")
        val valid = this.jwtTokenProvider.validateToken(request.token)
        return ResponseEntity.ok(TokenLoginResponse(valid))
    }
}