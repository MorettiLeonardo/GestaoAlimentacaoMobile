package com.example.gestaoalimentacao.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal object SessaoUsuario {
    var usuarioId by mutableStateOf<Long?>(null)
        private set

    fun iniciar(id: Long) {
        require(id > 0)
        usuarioId = id
    }

    fun encerrar() {
        usuarioId = null
    }

    fun exigirUsuario(): Long = requireNotNull(usuarioId) {
        "Faça login para continuar."
    }
}
