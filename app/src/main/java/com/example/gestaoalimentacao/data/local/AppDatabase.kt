package com.example.gestaoalimentacao.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

internal class AppDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    File(context.noBackupFilesDir, "gestao_alimentacao.db").absolutePath,
    null,
    4
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE usuarios(
            id INTEGER PRIMARY KEY,
            nome TEXT NOT NULL CHECK(length(nome) BETWEEN 3 AND 100),
            email TEXT NOT NULL UNIQUE CHECK(length(email) BETWEEN 3 AND 254),
            senha BLOB NOT NULL,
            salt BLOB NOT NULL,
            meta_calorica INTEGER NOT NULL DEFAULT 2000
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE refeicoes(
            id TEXT PRIMARY KEY,
            usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
            titulo TEXT NOT NULL,
            descricao TEXT NOT NULL,
            categoria TEXT NOT NULL,
            porcao REAL NOT NULL,
            unidade TEXT NOT NULL,
            calorias INTEGER NOT NULL,
            carboidratos REAL NOT NULL,
            proteinas REAL NOT NULL,
            gorduras REAL NOT NULL,
            data TEXT NOT NULL,
            consumida INTEGER NOT NULL,
            observacoes TEXT NOT NULL,
            lembrete INTEGER NOT NULL
            )
        """.trimIndent())

        // Ingredientes e modo de preparo ficam em TEXT, um item por linha.
        db.execSQL("""
            CREATE TABLE receitas(
            id TEXT PRIMARY KEY,
            titulo TEXT NOT NULL,
            descricao TEXT NOT NULL,
            tempo_preparo TEXT NOT NULL,
            calorias INTEGER NOT NULL,
            categoria TEXT NOT NULL,
            ingredientes TEXT NOT NULL,
            modo_preparo TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS refeicoes")
        db.execSQL("DROP TABLE IF EXISTS receitas")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }
}
