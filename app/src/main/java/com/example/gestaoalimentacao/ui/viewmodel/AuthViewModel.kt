package com.example.gestaoalimentacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestaoalimentacao.data.model.User

class AuthViewModel : ViewModel() {
    private val users = mutableListOf(
        User("1", "Usuário Exemplo", "usuario@email.com", "123456")
    )

    var currentUser: User? = null
        private set

    fun login(email: String, senha: String): Boolean {
        currentUser = users.find { it.email.equals(email, ignoreCase = true) && it.password == senha }
        return currentUser != null
    }

    fun register(nome: String, email: String, senha: String): Boolean {
        if (users.any { it.email.equals(email, ignoreCase = true) }) return false

        val user = User((users.size + 1).toString(), nome, email, senha)
        users.add(user)
        currentUser = user
        return true
    }

    fun logout() {
        currentUser = null
    }
}
