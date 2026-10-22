package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.MealDao
import com.example.gestaoalimentacao.data.dal.UserDao
import com.example.gestaoalimentacao.data.model.Meal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MealDao(application)
    private val usuarioDao = UserDao(application)
    var refeicoes by mutableStateOf<List<Meal>>(emptyList())
        private set
    var refeicao by mutableStateOf<Meal?>(null)
        private set
    var busca by mutableStateOf("")
        private set
    var caloriasConsumidas by mutableStateOf(0)
        private set
    var metaCalorica by mutableStateOf(0)
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
        refeicoes = withContext(Dispatchers.IO) { dao.listar(usuarioId, busca) }
        caloriasConsumidas = withContext(Dispatchers.IO) { dao.somarCaloriasConsumidas(usuarioId) }
        metaCalorica = withContext(Dispatchers.IO) { usuarioDao.buscarPorId(usuarioId)?.calorieGoal ?: 0 }
    }

    fun listar() = executar { usuarioId -> carregarLista(usuarioId) }

    fun mudarBusca(valor: String) {
        if (valor.length <= 100) {
            busca = valor
            listar()
        }
    }

    fun buscarPorId(id: String) = executar { usuarioId ->
        val encontrada = withContext(Dispatchers.IO) { dao.buscarPorId(usuarioId, id) }
        refeicao = requireNotNull(encontrada) { "Refeição não encontrada." }
    }

    fun salvar(
        titulo: String,
        categoria: String,
        porcaoTexto: String,
        unidade: String,
        caloriasTexto: String,
        carboidratosTexto: String,
        proteinasTexto: String,
        gordurasTexto: String,
        lembrete: Boolean,
        observacoes: String
    ) = executar { usuarioId ->
        require(titulo.trim().length in 2..100) { "Informe um nome entre 2 e 100 caracteres." }
        require(categoria.isNotBlank()) { "Selecione uma categoria." }
        require(unidade.isNotBlank()) { "Selecione a unidade da porção." }
        val porcao = requireNotNull(porcaoTexto.toDoubleOrNull()) { "Informe uma porção válida." }
        val calorias = requireNotNull(caloriasTexto.toIntOrNull()) { "Informe as calorias." }

        val dados = (refeicao ?: Meal(title = titulo.trim())).copy(
            title = titulo.trim(),
            category = categoria,
            portionValue = porcao,
            portionUnit = unidade,
            calories = calorias,
            carbs = carboidratosTexto.toDoubleOrNull() ?: 0.0,
            protein = proteinasTexto.toDoubleOrNull() ?: 0.0,
            fat = gordurasTexto.toDoubleOrNull() ?: 0.0,
            notes = observacoes.trim(),
            reminderEnabled = lembrete
        )

        withContext(Dispatchers.IO) {
            if (refeicao == null) dao.cadastrar(usuarioId, dados)
            else dao.alterar(usuarioId, dados)
        }
        salvo = true
    }

    fun alternarConsumida(selecionada: Meal) = executar { usuarioId ->
        withContext(Dispatchers.IO) {
            dao.alterar(usuarioId, selecionada.copy(isConsumed = !selecionada.isConsumed))
        }
        carregarLista(usuarioId)
    }

    fun excluir(id: String) = executar { usuarioId ->
        withContext(Dispatchers.IO) { dao.excluir(usuarioId, id) }
        salvo = true
    }

    fun consumirSalvo() {
        salvo = false
    }
}
