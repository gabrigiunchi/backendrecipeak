package com.micellaneous.recipeak.controller

import com.micellaneous.recipeak.BaseRestTest
import com.micellaneous.recipeak.config.security.JwtTokenProvider
import com.micellaneous.recipeak.constants.ApiUrls
import com.micellaneous.recipeak.model.enum.UserType
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get

@SpringBootTest
class AliveControllerTest : BaseRestTest() {

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun clear() {
        this.userDAO.deleteAll()
        this.userDAO.count()
        this.createMockUser("gabrigiunchi")
    }

    @Test
    fun `Should get the version`() {
        this.mockMvc.get(ApiUrls.ALIVE)
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("$.version", `is`("0.0.1-test")) } }
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should get the logged user`() {
        this.mockMvc.get("${ApiUrls.ALIVE}/me")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.username", `is`("gabrigiunchi")) }
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should say if I am an admin`() {
        this.mockMvc.get("${ApiUrls.ALIVE}/me/admin")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.isAdmin", `is`("true")) }
    }

    @Test
    @WithMockUser(username = "baseuser", password = "bbbb", authorities = ["USER"])
    fun `Should say if I am NOT an admin`() {
        this.createMockUser("baseuser", "bbbb", UserType.USER)
        this.mockMvc.get("${ApiUrls.ALIVE}/me/admin")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.isAdmin", `is`("false")) }
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should allow administrators to access secured endpoints`() {
        this.mockMvc.get("${ApiUrls.ALIVE}/secret")
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser(username = "baseuser", password = "bbbb", authorities = ["USER"])
    fun `Should forbid regular users to access secured endpoints`() {
        this.createMockUser("baseuser", "bbbb", UserType.USER)
        this.mockMvc.get("${ApiUrls.ALIVE}/secret")
            .andExpect { status { isForbidden() } }
    }
}