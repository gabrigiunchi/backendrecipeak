package com.micellaneous.recipeak.exception

class ResourceNotFoundException(message: String) : RuntimeException(message) {
    constructor(objectClass: Class<out Any>, id: String) : this("${objectClass.simpleName} #$id not found")
    constructor(objectClass: Class<out Any>, id: Int) : this(objectClass, id.toString())
}
