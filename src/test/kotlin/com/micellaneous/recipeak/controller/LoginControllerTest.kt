package com.micellaneous.recipeak.controller


import com.micellaneous.recipeak.BaseRestTest
import com.micellaneous.recipeak.config.security.JwtTokenProvider
import com.micellaneous.recipeak.constants.ApiUrls
import com.micellaneous.recipeak.model.dto.input.ValidateTokenRequest
import com.micellaneous.recipeak.model.dto.input.ValidateUserDTO
import com.micellaneous.recipeak.model.enum.UserType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

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

        mockMvc.post(ApiUrls.LOGIN)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(credentials)
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.user.id", `is`(user.id)) }
            .andExpect { jsonPath("$.user.name", `is`(user.name)) }
            .andExpect { jsonPath("$.user.surname", `is`(user.surname)) }
            .andExpect { jsonPath("$.user.username", `is`(user.username)) }
            .andExpect { jsonPath("$.user.email", `is`(user.email)) }
            .andExpect { jsonPath("$.token", notNullValue()) }
    }

    @Test
    fun `Should NOT log in a user with invalid username`() {
        val credentials = ValidateUserDTO("mario", "djksnkan")

        assertThat(this.userDAO.findByUsername(credentials.username) == null).isTrue()

        mockMvc.post(ApiUrls.LOGIN)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(credentials)
        }
            .andExpect { status { isUnauthorized() } }
            .andExpect { jsonPath("$.message", `is`("Invalid username/password supplied")) }
    }

    @Test
    fun `Should NOT log in a user with invalid password`() {
        val password = "aaaa"
        val user = this.createMockUser("djkasnjdna", password)
        val credentials = ValidateUserDTO(user.username, "mmkldamaldmak")

        mockMvc.post(ApiUrls.LOGIN)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(credentials)
        }
            .andExpect { status { isUnauthorized() } }
            .andExpect { jsonPath("$.message", `is`("Invalid username/password supplied")) }
    }

    @Test
    fun `Should say if a token is valid`() {
        val user = this.createMockUser("gabrigiunchi", "aaaa")
        this.userDAO.save(user)
        val token = this.tokenProvider.createToken(user.username, listOf(UserType.ADMINISTRATOR.name))

        mockMvc.post("${ApiUrls.LOGIN}/token")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(ValidateTokenRequest(token))
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.valid", `is`(true)) }
    }

    @Test
    fun `Should say if a token is NOT valid`() {
        val token = ValidateTokenRequest("dadsajs")
        mockMvc.post("${ApiUrls.LOGIN}/token")
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(token)
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.valid", `is`(false)) }
    }

    @Test
    fun `Should not log in a disabled user`() {
        val password = "aaaa"
        val user = this.createMockUser("fasfassd", password)
        val credentials = ValidateUserDTO(user.username, password)

        mockMvc.post(ApiUrls.LOGIN)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(credentials)
        }
            .andExpect { status { isOk() } }

        user.active = false
        this.userDAO.save(user)
        mockMvc.post(ApiUrls.LOGIN)
        {
            contentType = MediaType.APPLICATION_JSON
            content = json(credentials)
        }
            .andExpect { status { isUnauthorized() } }
            .andExpect { jsonPath("$.message", `is`("Invalid username/password supplied")) }
    }

}