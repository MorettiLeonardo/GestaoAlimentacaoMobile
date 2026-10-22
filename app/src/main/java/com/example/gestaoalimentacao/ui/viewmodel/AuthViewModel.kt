package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.auth.AuthManager
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.UserDao
import com.example.gestaoalimentacao.data.model.User
import com.example.gestaoalimentacao.validation.CadastroValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserDao(application)
    var carregando by mutableStateOf(false)
        private set
    var concluido by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
    var usuario by mutableStateOf<User?>(null)
        private set

    private fun executar(op: suspend () -> Boolean) {
        if (carregando || concluido) return
        carregando = true
        mensagem = null

        viewModelScope.launch {
            try {
                concluido = withContext(Dispatchers.IO) { op() }
                if (!concluido) mensagem = "E-mail ou senha incorretos."
            } catch (e: CancellationException) {
                throw e
            } catch (e: SQLiteConstraintException) {
                mensagem = "Este e-mail já está cadastrado."
            } catch (e: IllegalArgumentException) {
                mensagem = e.message
            } catch (e: Exception) {
                mensagem = "Ocorreu um erro inesperado."
            } finally {
                carregando = false
            }
        }
    }

    fun cadastrar(nome: String, email: String, senha: String) = executar {
        val nomeLimpo = nome.trim()
        val emailLimpo = email.trim().lowercase()

        CadastroValidator.validar(nomeLimpo, emailLimpo, senha)

        val salt = AuthManager.criarSalt()
        val senhaHash = AuthManager.gerarHash(senha, salt)
        try {
            val id = dao.cadastrar(nomeLimpo, emailLimpo, senhaHash, salt)
            SessaoUsuario.iniciar(id)
            true
        } finally {
            senhaHash.fill(0)
        }
    }

    fun login(email: String, senha: String) = executar {
        require(email.length <= 254 && senha.length in 6..128) {
            "E-mail ou senha incorretos."
        }

        val dados = dao.buscarSenha(email.trim().lowercase())
        val senhaHash = dados?.hash ?: ByteArray(32)
        val salt = dados?.salt ?: ByteArray(16)

        val idAutenticado = try {
            val autenticado = AuthManager.validarSenha(senha, salt, senhaHash)
            if (autenticado) dados?.usuarioID else null
        } finally {
            senhaHash.fill(0)
        }

        if (idAutenticado != null) {
            SessaoUsuario.iniciar(idAutenticado)
            true
        } else false
    }

    fun consumirResultado() {
        concluido = false
    }

    fun carregarUsuario() {
        val id = SessaoUsuario.usuarioId ?: return
        viewModelScope.launch {
            try {
                usuario = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mensagem = "Não foi possível carregar o usuário."
            }
        }
    }
}
