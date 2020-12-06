package com.micellaneous.recipeak.service

import com.micellaneous.recipeak.BaseTest
import com.micellaneous.recipeak.exception.BadRequestException
import com.micellaneous.recipeak.model.dto.input.ChangePasswordDTO
import com.micellaneous.recipeak.model.dto.input.ValidateUserDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException

class UserServiceTest : BaseTest() {

    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        this.userDAO.deleteAll()
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
    fun `Should check the password of a user`() {
        val user = this.createMockUser("gabrigiunchi", "ciao")
        assertThat(this.userService.checkPassword(user, "ciao")).isTrue()
        assertThat(this.userService.checkPassword(user, "cidasdasdasao")).isFalse()
    }

    @Test
    fun `Should modify the password of a user`() {
        val user = this.createMockUser("gabrigiunchi")
        this.userService.modifyPasswordOfUser(user, ChangePasswordDTO("aaaa", "bbbb"))
        assertThat(this.userService.checkPassword(user, "bbbb")).isTrue()
    }

    @Test
    fun `Should NOT modify the password of a user if the old password is wrong`() {
        val user = this.createMockUser("gabrigiunchi")
        val exception = assertThrows<BadRequestException> {
            this.userService.modifyPasswordOfUser(user, ChangePasswordDTO("dajhdajs", "bbbb"))
        }
        assertThat(exception.message).isEqualTo("Old password is incorrect")
    }
}