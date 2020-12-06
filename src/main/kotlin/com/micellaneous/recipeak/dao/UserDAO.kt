package com.micellaneous.recipeak.dao

import com.micellaneous.recipeak.model.AppUser
import org.springframework.data.repository.PagingAndSortingRepository

interface UserDAO : PagingAndSortingRepository<AppUser, Int> {

    fun findByUsername(username: String): AppUser?
}