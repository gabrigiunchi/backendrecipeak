package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.UserDAO
import com.micellaneous.recipeak.model.AppUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/alive")
class AliveController(userDAO: UserDAO) : BaseController(userDAO) {

    @Value("\${version}")
    private lateinit var version: String

    @GetMapping
    fun alive(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("version" to this.version))
    }

    @GetMapping("/me")
    fun whoAmI(): ResponseEntity<AppUser> = ResponseEntity.ok(this.getLoggedUser())

    @GetMapping("/me/admin")
    fun amIAdmin(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(
            mapOf(
                Pair("username", this.getLoggedUser().username),
                Pair("isAdmin", this.isAdmin().toString())
            )
        )

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/secret")
    fun secret(): ResponseEntity<String> =
        ResponseEntity.ok("This endpoint is for administrators only. If you are not this is a problem")

}