package com.example.gestaoalimentacao.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gestaoalimentacao.data.local.AppDatabase
import com.example.gestaoalimentacao.data.model.Meal

internal class MealDao(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf(
        "id", "titulo", "descricao", "categoria", "porcao", "unidade", "calorias",
        "carboidratos", "proteinas", "gorduras", "data", "consumida", "observacoes", "lembrete"
    )

    fun listar(usuarioId: Long, busca: String): List<Meal> = AppDatabase(appContext).use { db ->
        val termo = "%${busca.trim()}%"
        db.readableDatabase.query(
            "refeicoes",
            colunas,
            "usuario_id = ? AND (titulo LIKE ? OR descricao LIKE ? OR categoria LIKE ?)",
            arrayOf(usuarioId.toString(), termo, termo, termo),
            null, null,
            "rowid DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(lerRefeicao(cursor))
                }
            }
        }
    }

    fun buscarPorId(usuarioId: Long, id: String): Meal? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "refeicoes",
            colunas,
            "id = ? AND usuario_id = ?",
            arrayOf(id, usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerRefeicao(cursor) else null
        }
    }

    fun somarCaloriasConsumidas(usuarioId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "refeicoes",
            arrayOf("COALESCE(SUM(calorias), 0)"),
            "usuario_id = ? AND consumida = 1",
            arrayOf(usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun cadastrar(usuarioId: Long, refeicao: Meal) {
        AppDatabase(appContext).use { db ->
            val dados = montarDados(refeicao).apply {
                put("id", refeicao.id)
                put("usuario_id", usuarioId)
            }
            db.writableDatabase.insertOrThrow("refeicoes", null, dados)
        }
    }

    fun alterar(usuarioId: Long, refeicao: Meal) {
        AppDatabase(appContext).use { db ->
            val alterados = db.writableDatabase.update(
                "refeicoes",
                montarDados(refeicao),
                "id = ? AND usuario_id = ?",
                arrayOf(refeicao.id, usuarioId.toString())
            )
            require(alterados == 1) { "Erro ao alterar a refeição" }
        }
    }

    fun excluir(usuarioId: Long, id: String) {
        AppDatabase(appContext).use { db ->
            val excluidos = db.writableDatabase.delete(
                "refeicoes",
                "id = ? AND usuario_id = ?",
                arrayOf(id, usuarioId.toString())
            )
            require(excluidos == 1) { "Refeição não pode ser excluída" }
        }
    }

    private fun lerRefeicao(cursor: Cursor) = Meal(
        id = cursor.getString(0),
        title = cursor.getString(1),
        description = cursor.getString(2),
        category = cursor.getString(3),
        portionValue = cursor.getDouble(4),
        portionUnit = cursor.getString(5),
        calories = cursor.getInt(6),
        carbs = cursor.getDouble(7),
        protein = cursor.getDouble(8),
        fat = cursor.getDouble(9),
        dateText = cursor.getString(10),
        isConsumed = cursor.getInt(11) == 1,
        notes = cursor.getString(12),
        reminderEnabled = cursor.getInt(13) == 1
    )

    private fun montarDados(refeicao: Meal) = ContentValues().apply {
        put("titulo", refeicao.title)
        put("descricao", refeicao.description)
        put("categoria", refeicao.category)
        put("porcao", refeicao.portionValue)
        put("unidade", refeicao.portionUnit)
        put("calorias", refeicao.calories)
        put("carboidratos", refeicao.carbs)
        put("proteinas", refeicao.protein)
        put("gorduras", refeicao.fat)
        put("data", refeicao.dateText)
        put("consumida", refeicao.isConsumed)
        put("observacoes", refeicao.notes)
        put("lembrete", refeicao.reminderEnabled)
    }
}
