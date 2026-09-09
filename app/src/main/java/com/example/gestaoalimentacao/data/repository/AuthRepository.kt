package com.example.gestaoalimentacao.data.repository

import com.example.gestaoalimentacao.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthRepository {
    private val users = mutableListOf<User>(
        User("1", "Usuário Exemplo", "usuario@email.com", "123456")
    )

    private val _currentUser = MutableStateFlow<User?>(users.first())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(email: String, password: String): Result<User> {
        val user = users.find { it.email.equals(email, ignoreCase = true) && it.password == password }
        return if (user != null) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("E-mail ou senha incorretos."))
        }
    }

    fun register(name: String, email: String, password: String): Result<User> {
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(Exception("Este e-mail já está cadastrado."))
        }
        val newUser = User(
            id = (users.size + 1).toString(),
            name = name,
            email = email,
            password = password
        )
        users.add(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }
}
