package com.example.gestaoalimentacao.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.data.remote.MealDto
import com.example.gestaoalimentacao.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SuggestionsViewModel : ViewModel() {
    var busca by mutableStateOf("chicken")
        private set
    var resultados by mutableStateOf<List<MealDto>>(emptyList())
        private set
    var carregando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    fun mudarBusca(valor: String) {
        if (valor.length <= 50) busca = valor
    }

    fun buscar() {
        if (carregando) return
        val termo = busca.trim()
        if (termo.isEmpty()) {
            mensagem = "Digite algo para buscar."
            return
        }
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                val resposta = RetrofitClient.api.buscar(termo)
                resultados = resposta.meals ?: emptyList()
                if (resultados.isEmpty()) mensagem = "Nenhuma sugestão encontrada."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mensagem = "Não foi possível carregar. Verifique a internet."
            } finally {
                carregando = false
            }
        }
    }
}
