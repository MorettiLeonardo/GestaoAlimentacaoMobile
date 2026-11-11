package com.example.gestaoalimentacao.ui.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Looper
import org.junit.Assert.fail
import org.robolectric.Shadows.shadowOf
import java.io.File

/**
 * As ViewModels disparam corrotinas no viewModelScope (Main) e acessam o banco em
 * Dispatchers.IO. Este helper processa a fila da main thread do Robolectric até a
 * operação terminar (carregando == false), com um limite de tempo.
 */
internal fun aguardarOperacao(carregando: () -> Boolean, timeoutMs: Long = 5_000) {
    val limite = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < limite) {
        shadowOf(Looper.getMainLooper()).idle()
        if (!carregando()) return
        Thread.sleep(5)
    }
    fail("A operação da ViewModel não terminou em ${timeoutMs}ms")
}

/** Remove o banco usado pelo app para que cada teste comece do zero. */
internal fun apagarBanco(context: Context) {
    SQLiteDatabase.deleteDatabase(File(context.noBackupFilesDir, "gestao_alimentacao.db"))
}
