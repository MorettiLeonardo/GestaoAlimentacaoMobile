package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.WaterDao
import com.example.gestaoalimentacao.data.model.WaterIntake
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = WaterDao(application)
    var registros by mutableStateOf<List<WaterIntake>>(emptyList())
        private set
    var registro by mutableStateOf<WaterIntake?>(null)
        private set
    var totalMl by mutableStateOf(0)
        private set
    var carregando by mutableStateOf(false)
        private set
    var salvo by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    private fun executar(op: suspend (Long) -> Unit) {
        if (carregando || salvo) return
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                op(SessaoUsuario.exigirUsuario())
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mensagem = e.message
            } catch (e: Exception) {
                mensagem = "Um erro inesperado aconteceu."
            } finally {
                carregando = false
            }
        }
    }

    private suspend fun carregarLista(usuarioId: Long) {
        registros = withContext(Dispatchers.IO) { dao.listar(usuarioId) }
        totalMl = registros.sumOf { it.amountMl }
    }

    fun listar() = executar { usuarioId -> carregarLista(usuarioId) }

    fun buscarPorId(id: String) = executar { usuarioId ->
        val encontrado = withContext(Dispatchers.IO) { dao.buscarPorId(usuarioId, id) }
        registro = requireNotNull(encontrado) { "Registro não encontrado." }
    }

    fun salvar(quantidadeTexto: String, horario: String, anotacao: String) = executar { usuarioId ->
        val quantidade = requireNotNull(quantidadeTexto.trim().toIntOrNull()) {
            "Informe a quantidade em ml."
        }
        require(quantidade in 1..5000) { "A quantidade deve ficar entre 1 e 5000 ml." }
        require(horario.trim().matches(Regex("([01]\\d|2[0-3]):[0-5]\\d"))) {
            "Informe o horário no formato HH:mm."
        }
        require(anotacao.trim().length <= 100) { "A anotação pode ter até 100 caracteres." }

        val dados = (registro ?: WaterIntake(amountMl = quantidade)).copy(
            amountMl = quantidade,
            timeText = horario.trim(),
            note = anotacao.trim()
        )

        withContext(Dispatchers.IO) {
            if (registro == null) dao.cadastrar(usuarioId, dados)
            else dao.alterar(usuarioId, dados)
        }
        salvo = true
    }

    fun excluir(id: String) = executar { usuarioId ->
        withContext(Dispatchers.IO) { dao.excluir(usuarioId, id) }
        carregarLista(usuarioId)
    }

    fun consumirSalvo() {
        salvo = false
    }
       fun novoRegistro() {
        registro = null
    }
}