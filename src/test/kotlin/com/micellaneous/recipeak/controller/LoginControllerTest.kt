package com.micellaneous.recipeak.controller


import com.micellaneous.recipeak.BaseRestTest
import com.micellaneous.recipeak.config.security.JwtTokenProvider
import com.micellaneous.recipeak.constants.ApiUrls
import com.micellaneous.recipeak.model.dto.input.ValidateTokenRequest
import com.micellaneous.recipeak.model.dto.input.ValidateUserDTO
import com.micellaneous.recipeak.model.enum.UserType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class LoginControllerTest : BaseRestTest() {

    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @BeforeEach
    fun clearDB() {
        this.userDAO.deleteAll()
    }

    @Test
    fun `Should log in a user with valid username and password`() {
        val password = "aaaa"
        val user = this.createMockUser("dahdkahskd", password)

        val credentials = ValidateUserDTO(user.username, password)

        assertThat(this.userDAO.findByUsername(user.username) != null).isTrue()
        assertThat(this.userDAO.findByUsername(user.username)?.password).isEqualTo(user.password)
        assertThat(this.userDAO.findByUsername(user.username)?.active).isTrue()

        this.mockMvc.perform(
            post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(user.id)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.name", Matchers.`is`(user.name)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.surname", Matchers.`is`(user.surname)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", Matchers.`is`(user.username)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", Matchers.`is`(user.email)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token", Matchers.notNullValue()))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT log in a user with invalid username`() {
        val credentials = ValidateUserDTO("mario", "djksnkan")

        assertThat(this.userDAO.findByUsername(credentials.username) == null).isTrue()

        this.mockMvc.perform(
            post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.message",
                    Matchers.`is`("Invalid username/password supplied")
                )
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT log in a user with invalid password`() {
        val password = "aaaa"
        val user = this.createMockUser("djkasnjdna", password)
        val credentials = ValidateUserDTO(user.username, "mmkldamaldmak")

        this.mockMvc.perform(
            post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.message",
                    Matchers.`is`("Invalid username/password supplied")
                )
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should say if a token is valid`() {
        val user = this.createMockUser("gabrigiunchi", "aaaa")
        this.userDAO.save(user)
        val token = this.tokenProvider.createToken(user.username, listOf(UserType.ADMINISTRATOR.name))

        this.mockMvc.perform(
            post("${ApiUrls.LOGIN}/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(ValidateTokenRequest(token)))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid", Matchers.`is`(true)))
    }

    @Test
    fun `Should say if a token is NOT valid`() {
        val token = ValidateTokenRequest("dadsajs")
        this.mockMvc.perform(
            post("${ApiUrls.LOGIN}/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(token))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid", Matchers.`is`(false)))
    }

    @Test
    fun `Should not log in a disabled user`() {
        val password = "aaaa"
        val user = this.createMockUser("fasfassd", password)
        user.active = false
        this.userDAO.save(user)

        val credentials = ValidateUserDTO(user.username, password)

        this.mockMvc.perform(
            post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.message",
                    Matchers.`is`("Invalid username/password supplied")
                )
            )
    }

}