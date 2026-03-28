package me.hker.module.auth

open class AuthenticationException(message: String) : RuntimeException(message)

class InvalidCredentialsException : AuthenticationException("Invalid email or password")

class UserAlreadyExistsException(email: String) : AuthenticationException("User with email $email already exists")

class UserNotFoundException(email: String) : AuthenticationException("User not found: $email")
