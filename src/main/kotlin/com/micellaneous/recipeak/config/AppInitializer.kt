package com.micellaneous.recipeak.config

import com.micellaneous.recipeak.dao.UserDAO
import com.micellaneous.recipeak.model.AppUser
import com.micellaneous.recipeak.model.enum.UserType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AppInitializer(private val userDAO: UserDAO) {


    @Value("\${application.initDB}")
    private var initDB = false

    private val logger = LoggerFactory.getLogger(AppInitializer::class.java)

    fun initDB() {
        if (this.initDB) {
            this.logger.info("Creating entities")
            this.initUsers()
            this.logger.info("DB initialized")
        }
    }

    private fun initUsers() {
        this.logger.info("Creating users")
        this.userDAO.saveAll(
            listOf(
                AppUser(
                    "gabrigiunchi",
                    BCryptPasswordEncoder().encode("aaaa"),
                    "Gabriele",
                    "Giunchi",
                    "",
                    UserType.ADMINISTRATOR
                ),
                AppUser(
                    "amedeofrabris",
                    BCryptPasswordEncoder().encode("aaaa"),
                    "Amedeo",
                    "Fabris",
                    "",
                    UserType.ADMINISTRATOR
                )
            )
        )
        this.logger.info("Users created")
    }
}