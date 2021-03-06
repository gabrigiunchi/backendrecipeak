package com.micellaneous.recipeak.service

import com.micellaneous.recipeak.BaseTest
import com.micellaneous.recipeak.exception.BadRequestException
import com.micellaneous.recipeak.exception.ResourceNotFoundException
import com.micellaneous.recipeak.model.dto.input.ChangePasswordDTO
import com.micellaneous.recipeak.model.dto.input.UserDTOInput
import com.micellaneous.recipeak.model.dto.input.ValidateUserDTO
import com.micellaneous.recipeak.model.enum.UserType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import java.time.OffsetDateTime

class UserServiceTest : BaseTest() {

    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        this.userDAO.deleteAll()
    }

    @Test
    fun `Should get a page of users`() {
        val users = (1..10).map { this.createMockUser("user$it") }.toList()

        var result = this.userService.getUsersPaged(0, 2)
        assertThat(result.content.toList()).isEqualTo(users.take(2))

        result = this.userService.getUsersPaged(2, 3)
        assertThat(result.content.toList()).isEqualTo(listOf(users[6], users[7], users[8]))

        result = this.userService.getUsersPaged(0, 20)
        assertThat(result.content.toList()).isEqualTo(users)
    }

    @Test
    fun `Should get a user by username`() {
        val user = this.createMockUser("gabrigiunchi")
        val result = this.userService.getUser("gabrigiunchi")
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `Should get a user by id`() {
        val user = this.createMockUser("gabriginchi")
        val result = this.userService.getUser(user.id)
        assertThat(result).isEqualTo(user)

        val exception = assertThrows<ResourceNotFoundException> {
            this.userService.getUser(-1)
        }
        assertThat(exception.message).isEqualTo("AppUser #-1 not found")
    }

    @Test
    fun `Should authenticate a user`() {
        val user = this.createMockUser("gabrigiunchi")
        assertThat(this.userService.authenticate(ValidateUserDTO("gabrigiunchi", "aaaa")))
            .isEqualTo(user)
    }


    @Test
    fun `Should authenticate a user and thow an exception if the validation fails`() {
        val user = this.createMockUser("gabrigiunchi")
        var exception: BadCredentialsException = assertThrows {
            this.userService.authenticate(ValidateUserDTO("gabrigiunchi", "dasdas"))
        }
        assertThat(exception.message).isEqualTo("Invalid username/password supplied")

        exception = assertThrows {
            this.userService.authenticate(ValidateUserDTO("notexistent", "dasdas"))
        }
        assertThat(exception.message).isEqualTo("Invalid username/password supplied")

        user.active = false
        this.userDAO.save(user)
        exception = assertThrows {
            this.userService.authenticate(ValidateUserDTO("gabrigiunchi", "dasdas"))
        }
        assertThat(exception.message).isEqualTo("Invalid username/password supplied")
    }

    @Test
    fun `Should NOT authenticate a disabled user`() {
        this.createMockUser("gabrigiunchi", active = false)
        val exception: BadCredentialsException = assertThrows {
            this.userService.authenticate(ValidateUserDTO("gabrigiunchi", "aaaa"))
        }
        assertThat(exception.message).isEqualTo("Invalid username/password supplied")
    }

    @Test
    fun `Should NOT authenticate an expired user`() {
        this.createMockUser(
            "gabrigiunchi",
            validFrom = OffsetDateTime.now().minusDays(10),
            expireDate = OffsetDateTime.now().minusDays(1)
        )
        val exception: BadCredentialsException = assertThrows {
            this.userService.authenticate(ValidateUserDTO("gabrigiunchi", "aaaa"))
        }
        assertThat(exception.message).isEqualTo("Invalid username/password supplied")
    }

    @Test
    fun `Should say if a user is valid or not`() {
        var user = this.createMockUser("user1")
        assertThat(this.userService.isUserValid(user)).isTrue()

        user = this.createMockUser("user2", active = false)
        assertThat(this.userService.isUserValid(user)).isFalse()

        user = this.createMockUser(
            "user3",
            validFrom = OffsetDateTime.MIN,
            expireDate = OffsetDateTime.now().minusDays(1)
        )
        assertThat(this.userService.isUserValid(user)).isFalse()
    }

    @Test
    fun `Should check the password of a user`() {
        val user = this.createMockUser("gabrigiunchi", "ciao")
        assertThat(this.userService.checkPassword(user, "ciao")).isTrue()
        assertThat(this.userService.checkPassword(user, "cidasdasdasao")).isFalse()
    }

    @Test
    fun `Should modify the password of a user`() {
        val user = this.createMockUser("gabrigiunchi")
        this.userService.modifyPasswordOfUser(user.id, ChangePasswordDTO("aaaa", "bbbb"))
        assertThat(this.userService.checkPassword(user, "bbbb")).isTrue()
    }

    @Test
    fun `Should not modify the password of a user which doesn't exist`() {
        val exception = assertThrows<ResourceNotFoundException> {
            this.userService.modifyPasswordOfUser(-1, ChangePasswordDTO("aaaa", "bbbb"))
        }
        assertThat(exception.message).isEqualTo("AppUser #-1 not found")
    }

    @Test
    fun `Should NOT modify the password of a user if the old password is wrong`() {
        val user = this.createMockUser("gabrigiunchi")
        val exception = assertThrows<BadRequestException> {
            this.userService.modifyPasswordOfUser(user, ChangePasswordDTO("dajhdajs", "bbbb"))
        }
        assertThat(exception.message).isEqualTo("Old password is incorrect")
    }

    @Test
    fun `Should create a user`() {
        val dto = UserDTOInput(
            username = "user1",
            password = "pwd1",
            name = "User",
            surname = "Surname",
            email = "email1",
            UserType.USER
        )
        val result = this.userService.createUser(dto)
        assertThat(result.name).isEqualTo("User")
        assertThat(result.surname).isEqualTo("Surname")
        assertThat(result.email).isEqualTo("email1")
        assertThat(result.type).isEqualTo(UserType.USER)
        assertThat(result.password).isNotEqualTo("aaaa")
        assertThat(result.active).isEqualTo(true)
        assertThat(result.validFrom).isBeforeOrEqualTo(OffsetDateTime.now())
        assertThat(result.expireDate).isAfter(OffsetDateTime.now())
    }

    @Test
    fun `Should modify a user`() {
        val dto = UserDTOInput(
            username = "user1",
            password = "pwd1",
            name = "User",
            surname = "Surname",
            email = "email1",
            type = UserType.USER,
            isActive = false
        )
        val user = this.createMockUser("gabrigiunchi")
        val result = this.userService.modifyUser(dto, user.id)
        assertThat(result.name).isEqualTo("User")
        assertThat(result.surname).isEqualTo("Surname")
        assertThat(result.email).isEqualTo("email1")
        assertThat(result.type).isEqualTo(UserType.USER)
        assertThat(result.password).isNotEqualTo("aaaa")
        assertThat(result.active).isEqualTo(false)
    }

    @Test
    fun `Should NOT modify a user which does not exist`() {
        val dto = UserDTOInput(
            username = "user1",
            password = "pwd1",
            name = "User",
            surname = "Surname",
            email = "email1",
            type = UserType.USER,
            isActive = false
        )
        val exception = assertThrows<ResourceNotFoundException> {
            this.userService.modifyUser(dto, -1)
        }
        assertThat(exception.message).isEqualTo("AppUser #-1 not found")
    }
}