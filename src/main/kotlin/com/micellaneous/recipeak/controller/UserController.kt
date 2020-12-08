package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.dao.UserDAO
import com.micellaneous.recipeak.exception.ResourceAlreadyExistsException
import com.micellaneous.recipeak.exception.ResourceNotFoundException
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.dto.input.ChangePasswordDTO
import com.micellaneous.recipeak.model.dto.input.UserDTOInput
import com.micellaneous.recipeak.model.dto.output.UserDTOOutput
import com.micellaneous.recipeak.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userDAO: UserDAO, private val userService: UserService) : BaseController(userDAO) {

    val logger = LoggerFactory.getLogger(UserController::class.java)!!

    @GetMapping("/page/{page}/size/{size}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    fun getAllUsers(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<UserDTOOutput>> {
        this.logger.info("GET all users, page=$page size=$size")
        return ResponseEntity.ok(this.userService.getUsersPaged(page, size).map { e -> UserDTOOutput(e) })
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Int): ResponseEntity<AppUser> {
        this.logger.info("GET user #$id")
        return this.userDAO.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElseThrow { ResourceNotFoundException(AppUser::class.java, id) }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createUser(@Validated @RequestBody user: UserDTOInput): ResponseEntity<UserDTOOutput> {
        this.logger.info("POST a new user")
        this.logger.info(user.toString())

        if (this.userDAO.findByUsername(user.username) != null) {
            throw ResourceAlreadyExistsException("user with username ${user.username} already exists")
        }

        return ResponseEntity(UserDTOOutput(this.userDAO.save(this.userService.createUser(user))), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun modifyUser(@Validated @RequestBody user: UserDTOInput, @PathVariable id: Int): ResponseEntity<UserDTOOutput> {
        this.logger.info("PUT user $id")
        return ResponseEntity.ok(UserDTOOutput(this.userService.modifyUser(user, id)))
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE user #$id")
        val user = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException(AppUser::class.java, id) }
        this.userDAO.delete(user)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PatchMapping("/{id}/active/{active}")
    fun enableUser(@PathVariable id: Int, @PathVariable active: Boolean): ResponseEntity<Void> {
        this.logger.info("PATCH to set user #$id active=$active")
        val user = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException(AppUser::class.java, id) }
        user.active = active
        this.userDAO.save(user)
        return ResponseEntity(HttpStatus.OK)
    }

    /*************************************** ME ********************************************************************/

    @GetMapping("/me")
    fun getMyDetails(): ResponseEntity<UserDTOOutput> {
        val loggedUser = this.getLoggedUser()
        this.logger.info("GET logged user (#${loggedUser.id})")
        return ResponseEntity.ok(UserDTOOutput(loggedUser))
    }


    @PatchMapping("/me/password")
    fun changeMyPassword(@Validated @RequestBody dto: ChangePasswordDTO): ResponseEntity<UserDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("POST to change password of #${user.id}")
        return ResponseEntity.ok(UserDTOOutput(this.userService.modifyPasswordOfUser(user, dto)))
    }
}