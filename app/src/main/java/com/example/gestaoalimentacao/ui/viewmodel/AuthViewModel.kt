package com.example.gestaoalimentacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestaoalimentacao.data.model.User
import com.example.gestaoalimentacao.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    val currentUser: StateFlow<User?> = AuthRepository.currentUser

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, pass: String): Boolean {
        _errorMessage.value = null
        val result = AuthRepository.login(email, pass)
        return if (result.isSuccess) {
            true
        } else {
            _errorMessage.value = result.exceptionOrNull()?.message ?: "Erro ao entrar."
            false
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String): Boolean {
        _errorMessage.value = null
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Por favor, preencha todos os campos."
            return false
        }
        if (pass != confirmPass) {
            _errorMessage.value = "As senhas não coincidem."
            return false
        }
        val result = AuthRepository.register(name, email, pass)
        return if (result.isSuccess) {
            true
        } else {
            _errorMessage.value = result.exceptionOrNull()?.message ?: "Erro ao cadastrar."
            false
        }
    }

    fun logout() {
        AuthRepository.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
