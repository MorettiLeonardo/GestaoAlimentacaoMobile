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
        // Se o usuário digitou enquanto a consulta rodava, consulta de novo com o termo atual.
        var termo: String
        do {
            termo = busca
            refeicoes = withContext(Dispatchers.IO) { dao.listar(usuarioId, termo) }
        } while (termo != busca)
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
        val porcao = requireNotNull(lerDecimal(porcaoTexto)) { "Informe uma porção válida." }
        require(porcao > 0.0 && porcao <= MAX_PORCAO) { "A porção deve ficar entre 0 e ${MAX_PORCAO.toInt()}." }
        val calorias = requireNotNull(caloriasTexto.trim().toIntOrNull()) { "Informe as calorias." }
        require(calorias in 0..MAX_CALORIAS) { "As calorias devem ficar entre 0 e $MAX_CALORIAS." }
        val carboidratos = lerMacro(carboidratosTexto, "carboidratos")
        val proteinas = lerMacro(proteinasTexto, "proteínas")
        val gorduras = lerMacro(gordurasTexto, "gorduras")

        val dados = (refeicao ?: Meal(title = titulo.trim())).copy(
            title = titulo.trim(),
            category = categoria,
            portionValue = porcao,
            portionUnit = unidade,
            calories = calorias,
            carbs = carboidratos,
            protein = proteinas,
            fat = gorduras,
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

    companion object {
        const val MAX_CALORIAS = 10_000
        const val MAX_PORCAO = 10_000.0
        private const val MAX_MACRO = 1_000.0

        /** Aceita vírgula ou ponto como separador decimal; rejeita NaN/Infinity. */
        internal fun lerDecimal(texto: String): Double? =
            texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

        /** Campo de macro vazio vale 0; texto inválido ou fora da faixa gera erro em vez de virar 0. */
        private fun lerMacro(texto: String, nome: String): Double {
            if (texto.isBlank()) return 0.0
            val valor = requireNotNull(lerDecimal(texto)) { "Informe um valor válido para $nome." }
            require(valor in 0.0..MAX_MACRO) { "O valor de $nome deve ficar entre 0 e ${MAX_MACRO.toInt()} g." }
            return valor
        }
    }
}
