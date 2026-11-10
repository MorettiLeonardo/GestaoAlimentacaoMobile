package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.WeightDao
import com.example.gestaoalimentacao.data.model.WeightEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeightViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = WeightDao(application)
    var registros by mutableStateOf<List<WeightEntry>>(emptyList())
        private set
    var registro by mutableStateOf<WeightEntry?>(null)
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
    }

    fun listar() = executar { usuarioId -> carregarLista(usuarioId) }

    fun buscarPorId(id: String) = executar { usuarioId ->
        val encontrado = withContext(Dispatchers.IO) { dao.buscarPorId(usuarioId, id) }
        registro = requireNotNull(encontrado) { "Registro não encontrado." }
    }

    fun salvar(pesoTexto: String, data: String, foto: String) = executar { usuarioId ->
        val peso = requireNotNull(pesoTexto.trim().replace(',', '.').toDoubleOrNull()) {
            "Informe o peso em kg."
        }
        require(peso in 20.0..500.0) { "O peso deve ficar entre 20 e 500 kg." }
        require(data.trim().matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            "Informe a data no formato dd/MM/aaaa."
        }

        val dados = (registro ?: WeightEntry(weightKg = peso)).copy(
            weightKg = peso,
            dateText = data.trim(),
            photoPath = foto
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
