package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.BaseRestTest
import com.micellaneous.recipeak.constants.ApiUrls
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.dto.input.ChangePasswordDTO
import com.micellaneous.recipeak.model.dto.input.UserDTOInput
import com.micellaneous.recipeak.model.enum.UserType
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class UserControllerTest : BaseRestTest() {

    @BeforeEach
    fun clearDB() {
        this.userDAO.deleteAll()
    }

    @Test
    fun `Should get all users`() {
        this.userDAO.saveAll(
            listOf(
                AppUser("gabrigiunchi1", "dsndja", "Gabriele", "Giunchi", "mail@mail.com"),
                AppUser("fragiunchi", "dsndja", "Francesco", "Giunchi", "mail@mail.com"),
                AppUser("fabiogiunchi", "dsndja", "Fabio", "Giunchi", "mail@mail.com")
            )
        )
        mockMvc.get("${ApiUrls.USERS}/page/0/size/10")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.content.length()", `is`(3)) }
    }

    @Test
    fun `Should get a user given its id`() {
        val user = this.userDAO.save(AppUser("giggi", "ddnsakjn", "Gianni", "Riccio", "mail@mail.com"))
        mockMvc.get("${ApiUrls.USERS}/${user.id}")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.username", `is`(user.username)) }
            .andExpect { jsonPath("$.name", `is`(user.name)) }
            .andExpect { jsonPath("$.surname", `is`(user.surname)) }
    }

    @Test
    fun `Should not get a user if it does not exist`() {
        mockMvc.get("${ApiUrls.USERS}/-1")
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", `is`("AppUser #-1 not found")) }
    }

    @Test
    fun `Should create a user`() {
        val user = UserDTOInput("giggi", "ddnsakjn", "", "", "mail@mail.com", UserType.ADMINISTRATOR)
        mockMvc.post(ApiUrls.USERS)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(user)
        }
            .andExpect { status { isCreated() } }
            .andExpect { jsonPath("$.username", `is`(user.username)) }
    }

    @Test
    fun `Should not create a user if its username already exists`() {
        val user = this.createMockUser("gab", "aaaa")
        mockMvc.post(ApiUrls.USERS)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(UserDTOInput(user))
        }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `Should modify a user`() {
        val existing = this.createMockUser("gab", "aaaa")
        val oldPassword = existing.password
        val modified = UserDTOInput(
            "username",
            "password",
            "User",
            "Surname",
            "newmail",
            isActive = false,
            type = UserType.ADMINISTRATOR
        )

        mockMvc.put("${ApiUrls.USERS}/${existing.id}")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(modified)
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.name", `is`(modified.name)) }
            .andExpect { jsonPath("$.surname", `is`(modified.surname)) }
            .andExpect { jsonPath("$.email", `is`(modified.email)) }

        val result = this.userDAO.findById(existing.id).get()
        Assertions.assertThat(result.active).isFalse()
        Assertions.assertThat(result.password).isNotEqualTo(oldPassword)
    }

    @Test
    fun `Should not modify a user if it does not exist`() {
        val modified = UserDTOInput(
            "username", "password", "User", "Surname",
            "newmail", isActive = false, type = UserType.ADMINISTRATOR
        )

        mockMvc.put("${ApiUrls.USERS}/-1")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(modified)
        }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", `is`("AppUser #-1 not found")) }

    }

    @Test
    fun `Should delete a user`() {
        val user = this.createMockUser("fadsd", "dasda")
        mockMvc.perform(
            delete("${ApiUrls.USERS}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete a user if it does not exist`() {
        mockMvc.perform(
            delete("${ApiUrls.USERS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message", `is`("AppUser #-1 not found")))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should deactivate a user`() {
        val user = this.createMockUser("gabrigiunchi")
        Assertions.assertThat(user.active).isTrue()

        mockMvc.get("${ApiUrls.USERS}/${user.id}")
            .andExpect { status { isOk() } }

        mockMvc.patch("${ApiUrls.USERS}/${user.id}/active/false")
            .andExpect { status { isOk() } }

        Assertions.assertThat(this.userDAO.findByUsername(user.username)?.active).isFalse()
    }

    @Test
    fun `Should not deactivate a user if it does not exist`() {
        mockMvc.patch("${ApiUrls.USERS}/-1/active/false")
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", `is`("AppUser #-1 not found")) }
    }

    @Test
    fun `Should change the password of a user`() {
        val user = this.createMockUser("gabrigiunchi", "aaaa")
        val oldPassword = user.password
        val dto = ChangePasswordDTO("aaaa", "bbbb")
        mockMvc.patch("${ApiUrls.USERS}/${user.id}/password")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(dto)
        }.andExpect { status { isOk() } }

        val result = this.userDAO.findById(user.id).get()
        Assertions.assertThat(oldPassword).isNotEqualTo(result.password)
    }

    @Test
    fun `Should not change the password if the old password is incorrect`() {
        val user = this.createMockUser("gabrigiunchi")
        val oldPassword = user.password
        val dto = ChangePasswordDTO("acvd", "bbbb")
        mockMvc.patch("${ApiUrls.USERS}/${user.id}/password")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(dto)
        }
            .andExpect { status { isBadRequest() } }
            .andExpect { jsonPath("$.message", `is`("Old password is incorrect")) }

        val result = this.userDAO.findById(user.id).get()
        Assertions.assertThat(oldPassword).isEqualTo(result.password)
    }

    @Test
    @WithMockUser(username = "baseuser", password = "bbbb", authorities = ["USER"])
    fun `Should not change the password without the rights`() {
        this.createMockUser("baseuser", "bbbb", UserType.USER)
        val user = this.createMockUser("gabrigiunchi")
        val oldPassword = user.password
        val dto = ChangePasswordDTO("acvd", "bbbb")
        mockMvc.patch("${ApiUrls.USERS}/${user.id}/password")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(dto)
        }
            .andExpect { status { isForbidden() } }
            .andExpect {
                jsonPath(
                    "$.message",
                    `is`("You don't have the rights to access user ${user.id} information")
                )
            }

        val result = this.userDAO.findById(user.id).get()
        Assertions.assertThat(oldPassword).isEqualTo(result.password)
    }
}