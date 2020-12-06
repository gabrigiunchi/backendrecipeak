package com.micellaneous.recipeak

import com.micellaneous.recipeak.config.AppInitializer
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RecipeakApplication(private val appInitializer: AppInitializer) : CommandLineRunner {

    override fun run(vararg args: String?) {
        this.appInitializer.initDB()
    }
}

fun main(args: Array<String>) {
    runApplication<RecipeakApplication>(*args)
}
