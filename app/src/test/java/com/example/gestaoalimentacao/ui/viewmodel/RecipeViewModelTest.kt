package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.RecipeDao
import com.example.gestaoalimentacao.data.model.Recipe
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecipeViewModelTest {

    private lateinit var app: Application
    private lateinit var dao: RecipeDao

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        apagarBanco(app)
        SessaoUsuario.iniciar(1L)
        dao = RecipeDao(app)
    }

    @After
    fun tearDown() {
        SessaoUsuario.encerrar()
        apagarBanco(app)
    }

    private fun receita(id: String, titulo: String) = Recipe(
        id = id, title = titulo, description = "", prepTime = "10 min", calories = 100,
        category = "Vegetal", ingredients = listOf("a"), instructions = listOf("b")
    )

    @Test
    fun bancoNovo_naoTemReceitas() {
        val vm = RecipeViewModel(app)
        vm.listar(); aguardarOperacao({ vm.carregando })
        assertTrue(vm.receitas.isEmpty())
        assertEquals(null, vm.mensagem)
    }

    @Test
    fun salvar_separaItensPorLinhaIgnorandoVazias() {
        val vm = RecipeViewModel(app)
        vm.salvar(" Salada ", "desc", "10 min", "120", "Vegetal", "alface\n\n tomate \n", "lavar\ncortar")
        aguardarOperacao({ vm.carregando })

        assertTrue(vm.salvo)
        val salva = dao.listar("").single()
        assertEquals("Salada", salva.title)
        assertEquals(listOf("alface", " tomate "), salva.ingredients)
        assertEquals(listOf("lavar", "cortar"), salva.instructions)
    }

    @Test
    fun editar_alteraMesmaReceita() {
        dao.cadastrar(receita("r1", "Salada"))
        val vm = RecipeViewModel(app)

        vm.buscarPorId("r1"); aguardarOperacao({ vm.carregando })
        vm.salvar("Salada 2", "", "5", "100", "Vegetal", "a", "b"); aguardarOperacao({ vm.carregando })

        assertEquals("Salada 2", dao.buscarPorId("r1")!!.title)
        assertEquals(1, dao.listar("").size)
    }

    @Test
    fun excluir_removeERecarregaLista() {
        dao.cadastrar(receita("r1", "Salada"))
        dao.cadastrar(receita("r2", "Sopa"))
        val vm = RecipeViewModel(app)

        vm.excluir("r1"); aguardarOperacao({ vm.carregando })

        assertEquals("Receita excluída.", vm.mensagem)
        assertEquals(listOf("r2"), vm.receitas.map { it.id })
    }

    @Test
    fun validacoes_mostramMensagens() {
        val vm = RecipeViewModel(app)
        fun salvar(titulo: String = "Ab", tempo: String = "1", calorias: String = "1",
                   categoria: String = "x", ingredientes: String = "a", modo: String = "b") {
            vm.salvar(titulo, "", tempo, calorias, categoria, ingredientes, modo)
            aguardarOperacao({ vm.carregando })
            assertFalse(vm.salvo)
        }
        salvar(titulo = "A"); assertEquals("Informe um nome entre 2 e 100 caracteres.", vm.mensagem)
        salvar(categoria = " "); assertEquals("Informe a categoria.", vm.mensagem)
        salvar(tempo = " "); assertEquals("Informe o tempo de preparo.", vm.mensagem)
        salvar(calorias = ""); assertEquals("Informe as calorias.", vm.mensagem)
        salvar(ingredientes = "\n \n"); assertEquals("Informe ao menos um ingrediente.", vm.mensagem)
        salvar(modo = ""); assertEquals("Informe ao menos um passo do modo de preparo.", vm.mensagem)
        assertTrue(dao.listar("").isEmpty())
    }

    @Test
    fun busca_digitadaDuranteCarregamento_eAplicadaAoFinal() {
        dao.cadastrar(receita("r1", "Arroz"))
        dao.cadastrar(receita("r2", "Frango"))
        val vm = RecipeViewModel(app)

        vm.mudarBusca("A")
        Thread.sleep(300)
        assertTrue(vm.carregando)
        vm.mudarBusca("Fr")
        aguardarOperacao({ vm.carregando })

        assertEquals(listOf("r2"), vm.receitas.map { it.id })
    }
}
