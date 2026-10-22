package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.RecipeDao
import com.example.gestaoalimentacao.data.model.Recipe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RecipeDao(application)
    var receitas by mutableStateOf<List<Recipe>>(emptyList())
        private set
    var receita by mutableStateOf<Recipe?>(null)
        private set
    var busca by mutableStateOf("")
        private set
    var carregando by mutableStateOf(false)
        private set
    var salvo by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    private fun executar(op: suspend () -> Unit) {
        if (carregando || salvo) return
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                SessaoUsuario.exigirUsuario()
                op()
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

    fun listar() = executar {
        receitas = withContext(Dispatchers.IO) { dao.listar(busca) }
    }

    fun mudarBusca(valor: String) {
        if (valor.length <= 100) {
            busca = valor
            listar()
        }
    }

    fun buscarPorId(id: String) = executar {
        val encontrada = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
        receita = requireNotNull(encontrada) { "Receita não encontrada." }
    }

    fun salvar(
        titulo: String,
        descricao: String,
        tempoPreparo: String,
        caloriasTexto: String,
        categoria: String,
        ingredientesTexto: String,
        modoPreparoTexto: String
    ) = executar {
        // Um item por linha, ignorando linhas vazias.
        val ingredientes = ingredientesTexto.lines().filter { it.isNotBlank() }
        val modoPreparo = modoPreparoTexto.lines().filter { it.isNotBlank() }

        require(titulo.trim().length in 2..100) { "Informe um nome entre 2 e 100 caracteres." }
        require(categoria.isNotBlank()) { "Informe a categoria." }
        require(tempoPreparo.isNotBlank()) { "Informe o tempo de preparo." }
        val calorias = requireNotNull(caloriasTexto.toIntOrNull()) { "Informe as calorias." }
        require(ingredientes.isNotEmpty()) { "Informe ao menos um ingrediente." }
        require(modoPreparo.isNotEmpty()) { "Informe ao menos um passo do modo de preparo." }

        val dados = Recipe(
            id = receita?.id ?: UUID.randomUUID().toString(),
            title = titulo.trim(),
            description = descricao.trim(),
            prepTime = tempoPreparo.trim(),
            calories = calorias,
            category = categoria.trim(),
            ingredients = ingredientes,
            instructions = modoPreparo
        )

        withContext(Dispatchers.IO) {
            if (receita == null) dao.cadastrar(dados) else dao.alterar(dados)
        }
        salvo = true
    }

    fun excluir(id: String) = executar {
        receitas = withContext(Dispatchers.IO) {
            dao.excluir(id)
            dao.listar(busca)
        }
        mensagem = "Receita excluída."
    }

    fun consumirSalvo() {
        salvo = false
    }
}
