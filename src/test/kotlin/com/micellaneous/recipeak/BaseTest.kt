package com.micellaneous.recipeak

import com.micellaneous.recipeak.dao.UserDAO
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.enum.UserType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import javax.transaction.Transactional

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BaseTest {

    @Autowired
    protected lateinit var userDAO: UserDAO

    fun createMockUser(
        username: String,
        password: String = "aaaa",
        type: UserType = UserType.ADMINISTRATOR,
        name: String = "aaaa",
        surname: String = "bbbb",
        active: Boolean = true
    ): AppUser {
        val user = AppUser(
            username = username,
            password = BCryptPasswordEncoder().encode(password),
            name = name,
            surname = surname,
            email = "",
            type = type
        )
        user.active = active
        return this.userDAO.save(user)
    }
}