package com.example.gestaoalimentacao.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gestaoalimentacao.data.local.AppDatabase
import com.example.gestaoalimentacao.data.model.WaterIntake

internal class WaterDao(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf("id", "quantidade_ml", "horario", "anotacao")

    fun listar(usuarioId: Long): List<WaterIntake> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "agua",
            colunas,
            "usuario_id = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "rowid DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(lerRegistro(cursor))
                }
            }
        }
    }

    fun buscarPorId(usuarioId: Long, id: String): WaterIntake? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "agua",
            colunas,
            "id = ? AND usuario_id = ?",
            arrayOf(id, usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerRegistro(cursor) else null
        }
    }

    fun cadastrar(usuarioId: Long, registro: WaterIntake) {
        AppDatabase(appContext).use { db ->
            val dados = montarDados(registro).apply {
                put("id", registro.id)
                put("usuario_id", usuarioId)
            }
            db.writableDatabase.insertOrThrow("agua", null, dados)
        }
    }

    fun alterar(usuarioId: Long, registro: WaterIntake) {
        AppDatabase(appContext).use { db ->
            val alterados = db.writableDatabase.update(
                "agua",
                montarDados(registro),
                "id = ? AND usuario_id = ?",
                arrayOf(registro.id, usuarioId.toString())
            )
            require(alterados == 1) { "Erro ao alterar o registro de água" }
        }
    }

    fun excluir(usuarioId: Long, id: String) {
        AppDatabase(appContext).use { db ->
            val excluidos = db.writableDatabase.delete(
                "agua",
                "id = ? AND usuario_id = ?",
                arrayOf(id, usuarioId.toString())
            )
            require(excluidos == 1) { "Registro de água não pode ser excluído" }
        }
    }

    private fun lerRegistro(cursor: Cursor) = WaterIntake(
        id = cursor.getString(0),
        amountMl = cursor.getInt(1),
        timeText = cursor.getString(2),
        note = cursor.getString(3)
    )

    private fun montarDados(registro: WaterIntake) = ContentValues().apply {
        put("quantidade_ml", registro.amountMl)
        put("horario", registro.timeText)
        put("anotacao", registro.note)
    }
}