package com.micellaneous.recipeak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.io.IOException

@AutoConfigureMockMvc
@WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
class BaseRestTest : BaseTest() {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Throws(IOException::class)
    protected fun json(o: Any): String {
        return ObjectMapper().registerModule(KotlinModule()).writeValueAsString(o)
    }
}