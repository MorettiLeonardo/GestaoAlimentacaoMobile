package com.example.gestaoalimentacao.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gestaoalimentacao.data.local.AppDatabase
import com.example.gestaoalimentacao.data.model.Recipe

internal class RecipeDao(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf(
        "id", "titulo", "descricao", "tempo_preparo", "calorias", "categoria", "ingredientes", "modo_preparo"
    )

    fun listar(busca: String): List<Recipe> = AppDatabase(appContext).use { db ->
        val termo = "%${busca.trim()}%"
        db.readableDatabase.query(
            "receitas",
            colunas,
            "titulo LIKE ? OR descricao LIKE ? OR categoria LIKE ?",
            arrayOf(termo, termo, termo),
            null, null,
            "titulo COLLATE NOCASE"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(lerReceita(cursor))
                }
            }
        }
    }

    fun buscarPorId(id: String): Recipe? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "receitas",
            colunas,
            "id = ?",
            arrayOf(id),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerReceita(cursor) else null
        }
    }

    fun cadastrar(receita: Recipe) {
        AppDatabase(appContext).use { db ->
            val dados = montarDados(receita).apply { put("id", receita.id) }
            db.writableDatabase.insertOrThrow("receitas", null, dados)
        }
    }

    fun alterar(receita: Recipe) {
        AppDatabase(appContext).use { db ->
            val alterados = db.writableDatabase.update(
                "receitas",
                montarDados(receita),
                "id = ?",
                arrayOf(receita.id)
            )
            require(alterados == 1) { "Erro ao alterar a receita" }
        }
    }

    fun excluir(id: String) {
        AppDatabase(appContext).use { db ->
            val excluidos = db.writableDatabase.delete("receitas", "id = ?", arrayOf(id))
            require(excluidos == 1) { "Receita não pode ser excluída" }
        }
    }

    private fun lerReceita(cursor: Cursor) = Recipe(
        id = cursor.getString(0),
        title = cursor.getString(1),
        description = cursor.getString(2),
        prepTime = cursor.getString(3),
        calories = cursor.getInt(4),
        category = cursor.getString(5),
        ingredients = cursor.getString(6).lines(),
        instructions = cursor.getString(7).lines()
    )

    private fun montarDados(receita: Recipe) = ContentValues().apply {
        put("titulo", receita.title)
        put("descricao", receita.description)
        put("tempo_preparo", receita.prepTime)
        put("calorias", receita.calories)
        put("categoria", receita.category)
        put("ingredientes", receita.ingredients.joinToString("\n"))
        put("modo_preparo", receita.instructions.joinToString("\n"))
    }
}
