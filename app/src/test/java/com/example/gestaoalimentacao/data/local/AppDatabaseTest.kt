package com.example.gestaoalimentacao.data.local

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.data.dal.MealDao
import com.example.gestaoalimentacao.data.dal.WaterDao
import com.example.gestaoalimentacao.data.model.Meal
import com.example.gestaoalimentacao.data.model.WaterIntake
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseTest {

    private lateinit var app: Application
    private lateinit var arquivo: File

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        arquivo = File(app.noBackupFilesDir, "gestao_alimentacao.db")
        SQLiteDatabase.deleteDatabase(arquivo)
    }

    @After
    fun tearDown() {
        SQLiteDatabase.deleteDatabase(arquivo)
    }

    @Test
    fun bancoComVersaoMaisNova_eRecriadoEmVezDeFalhar() {
        // Simula instalar por cima uma versão do app com banco mais antigo.
        SQLiteDatabase.openOrCreateDatabase(arquivo, null).use { it.version = 99 }

        val dao = MealDao(app)
        assertTrue(dao.listar(1L, "").isEmpty())
        dao.cadastrar(1L, Meal(id = "x", title = "Ovo"))
        assertEquals(1, dao.listar(1L, "").size)
    }

    @Test
    fun upgradeDaMesmaVersao_naoApagaDados() {
        val dao = MealDao(app)
        dao.cadastrar(1L, Meal(id = "x", title = "Ovo"))

        // Reabrir o banco na versão atual não pode apagar nada.
        AppDatabase(app).use { it.readableDatabase }

        assertEquals(1, dao.listar(1L, "").size)
    }

    @Test
    fun checkDoBanco_bloqueiaQuantidadeDeAguaInvalida() {
        try {
            WaterDao(app).cadastrar(1L, WaterIntake(amountMl = 0))
            fail("O CHECK da tabela agua deveria rejeitar 0 ml")
        } catch (e: SQLiteConstraintException) {
            // esperado
        }
    }
}
