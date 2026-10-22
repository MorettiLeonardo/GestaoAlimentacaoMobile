package com.example.gestaoalimentacao.validation

import android.util.Patterns

object CadastroValidator {
    fun validar(nome: String, email: String, senha: String) {
        require(email.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Informe um e-mail válido."
        }
        require(nome.length in 3..100) {
            "Informe um nome entre 3 e 100 caracteres."
        }
        require(senha.length in 6..128 && senha.isNotBlank()) {
            "Use uma senha entre 6 e 128 caracteres."
        }
    }
}
