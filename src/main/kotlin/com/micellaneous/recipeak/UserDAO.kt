package com.micellaneous.recipeak

import com.micellaneous.recipeak.model.AppUser
import org.springframework.data.repository.PagingAndSortingRepository

interface UserDAO : PagingAndSortingRepository<AppUser, Int> {

    fun findByUsername(username: String): AppUser?
}