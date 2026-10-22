package com.example.gestaoalimentacao.data.dal

import android.content.ContentValues
import android.content.Context
import com.example.gestaoalimentacao.data.local.AppDatabase
import com.example.gestaoalimentacao.data.model.User

internal class SenhaSalva(val hash: ByteArray, val salt: ByteArray, val usuarioID: Long)

internal class UserDao(context: Context) {
    private val appContext = context.applicationContext

    fun cadastrar(nome: String, email: String, senha: ByteArray, salt: ByteArray): Long {
        return AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("nome", nome)
                put("email", email)
                put("senha", senha)
                put("salt", salt)
            }
            db.writableDatabase.insertOrThrow("usuarios", null, dados)
        }
    }

    fun buscarSenha(email: String): SenhaSalva? {
        return AppDatabase(appContext).use { db ->
            db.readableDatabase.query(
                "usuarios",
                arrayOf("senha", "salt", "id"),
                "email = ?",
                arrayOf(email),
                null, null, null
            ).use { usuario ->
                if (usuario.moveToFirst()) SenhaSalva(
                    usuario.getBlob(0),
                    usuario.getBlob(1),
                    usuario.getLong(2)
                ) else null
            }
        }
    }

    fun buscarPorId(id: Long): User? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios",
            arrayOf("id", "nome", "email", "meta_calorica"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) User(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getInt(3)
            ) else null
        }
    }
}
