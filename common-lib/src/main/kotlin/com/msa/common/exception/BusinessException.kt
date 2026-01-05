package com.msa.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val code: String,
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

class NotFoundException(
    resource: String,
    id: Any
) : BusinessException(
    code = "NOT_FOUND",
    message = "$resource not found with id: $id",
    status = HttpStatus.NOT_FOUND
)

class UnauthorizedException(
    message: String = "Unauthorized"
) : BusinessException(
    code = "UNAUTHORIZED",
    message = message,
    status = HttpStatus.UNAUTHORIZED
)

class ForbiddenException(
    message: String = "Access denied"
) : BusinessException(
    code = "FORBIDDEN",
    message = message,
    status = HttpStatus.FORBIDDEN
)

class ConflictException(
    message: String
) : BusinessException(
    code = "CONFLICT",
    message = message,
    status = HttpStatus.CONFLICT
)

class BadRequestException(
    message: String
) : BusinessException(
    code = "BAD_REQUEST",
    message = message,
    status = HttpStatus.BAD_REQUEST
)
