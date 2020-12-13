package com.micellaneous.recipeak.config

import com.micellaneous.recipeak.exception.AccessDeniedException
import com.micellaneous.recipeak.exception.BadRequestException
import com.micellaneous.recipeak.exception.ResourceAlreadyExistsException
import com.micellaneous.recipeak.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*

data class ErrorMessage(val logref: String, val message: String)

@ControllerAdvice
@RequestMapping
class ResourceControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun resourceNotFoundExceptionHandler(ex: ResourceNotFoundException): ErrorMessage {
        return ErrorMessage("Not Found Error", ex.message!!)
    }

    @ResponseBody
    @ExceptionHandler(ResourceAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun resourceAlreadyExistsExceptionHandler(ex: ResourceAlreadyExistsException): ErrorMessage {
        return ErrorMessage("Already Exist Error", ex.message!!)
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun badRequestExceptionHandler(ex: BadRequestException): ErrorMessage {
        return ErrorMessage("Bad Request", ex.message!!)
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessDeniedExceptionHandler(ex: AccessDeniedException): ErrorMessage {
        return ErrorMessage("Forbidden", ex.message!!)
    }

    @ResponseBody
    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun badCredentialsExceptionHandler(ex: BadCredentialsException): ErrorMessage {
        return ErrorMessage("Bad credentials", ex.message!!)
    }
}