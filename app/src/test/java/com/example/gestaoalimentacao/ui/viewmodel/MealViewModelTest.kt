package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.MealDao
import com.example.gestaoalimentacao.data.model.Meal
import com.example.gestaoalimentacao.ui.screens.formatarNumero
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val USUARIO = 1L
private const val OUTRO_USUARIO = 2L

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MealViewModelTest {

    private lateinit var app: Application
    private lateinit var dao: MealDao
    private lateinit var vm: MealViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        apagarBanco(app)
        SessaoUsuario.iniciar(USUARIO)
        dao = MealDao(app)
        vm = MealViewModel(app)
    }

    @After
    fun tearDown() {
        SessaoUsuario.encerrar()
        apagarBanco(app)
    }

    private fun aguardar() = aguardarOperacao({ vm.carregando })

    private fun salvar(
        titulo: String = "Frango",
        categoria: String = "Proteina",
        porcao: String = "150",
        unidade: String = "g",
        calorias: String = "300",
        carboidratos: String = "10",
        proteinas: String = "30",
        gorduras: String = "5",
        observacoes: String = " obs "
    ) {
        vm.salvar(titulo, categoria, porcao, unidade, calorias, carboidratos, proteinas, gorduras, true, observacoes)
        aguardar()
    }

    private fun assertErroSemPersistir(mensagemEsperada: String) {
        assertEquals(mensagemEsperada, vm.mensagem)
        assertFalse(vm.salvo)
        assertTrue(dao.listar(USUARIO, "").isEmpty())
    }

    // ---------- Cadastro e listagem ----------

    @Test
    fun salvar_dadosValidos_cadastraComTextoAparado() {
        salvar(titulo = "  Frango  ")

        assertTrue(vm.salvo)
        val salva = dao.listar(USUARIO, "").single()
        assertEquals("Frango", salva.title)
        assertEquals("obs", salva.notes)
        assertEquals(150.0, salva.portionValue, 0.0)
        assertEquals(300, salva.calories)
    }

    @Test
    fun alternarConsumida_atualizaSomaDeCalorias() {
        dao.cadastrar(USUARIO, Meal(id = "a", title = "Frango", calories = 300))

        vm.listar(); aguardar()
        assertEquals(0, vm.caloriasConsumidas)

        vm.alternarConsumida(vm.refeicoes.single()); aguardar()
        assertEquals(300, vm.caloriasConsumidas)
        assertTrue(vm.refeicoes.single().isConsumed)

        vm.alternarConsumida(vm.refeicoes.single()); aguardar()
        assertEquals(0, vm.caloriasConsumidas)
    }

    // ---------- Validação ----------

    @Test
    fun salvar_camposObrigatorios_mostramErro() {
        salvar(titulo = "A"); assertErroSemPersistir("Informe um nome entre 2 e 100 caracteres.")
        salvar(categoria = ""); assertErroSemPersistir("Selecione uma categoria.")
        salvar(unidade = ""); assertErroSemPersistir("Selecione a unidade da porção.")
        salvar(porcao = ""); assertErroSemPersistir("Informe uma porção válida.")
        salvar(calorias = ""); assertErroSemPersistir("Informe as calorias.")
    }

    @Test
    fun salvar_virgulaComoSeparadorDecimal_eAceita() {
        salvar(porcao = "1,5", carboidratos = "10,5", proteinas = "20,25", gorduras = "3,2")

        assertTrue(vm.salvo)
        val salva = dao.listar(USUARIO, "").single()
        assertEquals(1.5, salva.portionValue, 0.0)
        assertEquals(10.5, salva.carbs, 0.0)
        assertEquals(20.25, salva.protein, 0.0)
        assertEquals(3.2, salva.fat, 0.0)
    }

    @Test
    fun salvar_macrosVazios_valemZero() {
        salvar(carboidratos = "", proteinas = " ", gorduras = "")

        assertTrue(vm.salvo)
        val salva = dao.listar(USUARIO, "").single()
        assertEquals(0.0, salva.carbs, 0.0)
        assertEquals(0.0, salva.protein, 0.0)
        assertEquals(0.0, salva.fat, 0.0)
    }

    @Test
    fun salvar_macroInvalido_mostraErroEmVezDeGravarZero() {
        salvar(carboidratos = "abc")
        assertErroSemPersistir("Informe um valor válido para carboidratos.")
        salvar(gorduras = "1.2.3")
        assertErroSemPersistir("Informe um valor válido para gorduras.")
    }

    @Test
    fun salvar_valoresNegativosOuForaDaFaixa_saoRejeitados() {
        salvar(calorias = "-500"); assertErroSemPersistir("As calorias devem ficar entre 0 e 10000.")
        salvar(calorias = "10001"); assertErroSemPersistir("As calorias devem ficar entre 0 e 10000.")
        salvar(porcao = "0"); assertErroSemPersistir("A porção deve ficar entre 0 e 10000.")
        salvar(porcao = "-10"); assertErroSemPersistir("A porção deve ficar entre 0 e 10000.")
        salvar(proteinas = "-1"); assertErroSemPersistir("O valor de proteínas deve ficar entre 0 e 1000 g.")
    }

    @Test
    fun salvar_valoresNosLimites_saoAceitos() {
        salvar(calorias = "0", porcao = "10000", carboidratos = "1000")
        assertTrue(vm.salvo)
    }

    @Test
    fun salvar_NaNOuInfinito_eRejeitado() {
        salvar(porcao = "NaN"); assertErroSemPersistir("Informe uma porção válida.")
        salvar(porcao = "Infinity"); assertErroSemPersistir("Informe uma porção válida.")
        salvar(gorduras = "NaN"); assertErroSemPersistir("Informe um valor válido para gorduras.")
    }

    // ---------- Busca ----------

    @Test
    fun busca_filtraPorTextoEIsolaUsuario() {
        dao.cadastrar(USUARIO, Meal(id = "a", title = "Arroz integral", category = "Carboidrato"))
        dao.cadastrar(USUARIO, Meal(id = "b", title = "Frango", category = "Proteina"))
        dao.cadastrar(OUTRO_USUARIO, Meal(id = "c", title = "Arroz branco"))

        vm.mudarBusca("arroz"); aguardar()

        assertEquals(listOf("a"), vm.refeicoes.map { it.id })
    }

    @Test
    fun busca_digitadaDuranteCarregamento_eAplicadaAoFinal() {
        dao.cadastrar(USUARIO, Meal(id = "a", title = "Arroz"))
        dao.cadastrar(USUARIO, Meal(id = "b", title = "Frango"))

        vm.mudarBusca("A")
        Thread.sleep(300) // a consulta em IO já leu "A"; a main thread ainda não processou o resultado
        assertTrue(vm.carregando)
        vm.mudarBusca("Fr")
        aguardar()

        assertEquals("Fr", vm.busca)
        assertEquals(listOf("b"), vm.refeicoes.map { it.id })
    }

    // ---------- Edição e exclusão ----------

    @Test
    fun editar_preservaIdECasasDecimais() {
        dao.cadastrar(USUARIO, Meal(id = "x", title = "Ovo", portionValue = 150.5))

        vm.buscarPorId("x"); aguardar()
        val textoPorcao = formatarNumero(vm.refeicao!!.portionValue) // como a tela preenche o campo
        assertEquals("150.5", textoPorcao)

        salvar(titulo = "Ovo cozido", porcao = textoPorcao)

        assertTrue(vm.salvo)
        val editada = dao.buscarPorId(USUARIO, "x")!!
        assertEquals("Ovo cozido", editada.title)
        assertEquals(150.5, editada.portionValue, 0.0)
        assertEquals(1, dao.listar(USUARIO, "").size)
    }

    @Test
    fun formatarNumero_inteiroSemCasaDecimal() {
        assertEquals("150", formatarNumero(150.0))
        assertEquals("0", formatarNumero(0.0))
        assertEquals("10.25", formatarNumero(10.25))
    }

    @Test
    fun excluir_removeRegistro() {
        dao.cadastrar(USUARIO, Meal(id = "x", title = "Ovo"))

        vm.excluir("x"); aguardar()

        assertTrue(vm.salvo)
        assertNull(dao.buscarPorId(USUARIO, "x"))
    }

    @Test
    fun excluir_inexistenteOuDeOutroUsuario_mostraErro() {
        dao.cadastrar(OUTRO_USUARIO, Meal(id = "alheia", title = "Ovo"))

        vm.excluir("alheia"); aguardar()

        assertEquals("Refeição não pode ser excluída", vm.mensagem)
        assertEquals(1, dao.listar(OUTRO_USUARIO, "").size)
    }

    // ---------- Dados legados / limites do banco ----------

    @Test
    fun categoriaAntigaComErroDeDigitacao_eLidaCorrigida() {
        dao.cadastrar(USUARIO, Meal(id = "x", title = "Arroz", category = "Carboidrate"))
        assertEquals("Carboidrato", dao.buscarPorId(USUARIO, "x")!!.category)
    }

    @Test
    fun somaDeCaloriasMuitoGrande_naoFicaNegativa() {
        dao.cadastrar(USUARIO, Meal(id = "1", title = "x", calories = 2_000_000_000, isConsumed = true))
        dao.cadastrar(USUARIO, Meal(id = "2", title = "y", calories = 2_000_000_000, isConsumed = true))

        assertEquals(Int.MAX_VALUE, dao.somarCaloriasConsumidas(USUARIO))
    }

    @Test
    fun semUsuarioLogado_exibeMensagemDeLogin() {
        SessaoUsuario.encerrar()
        vm.listar(); aguardar()
        assertEquals("Faça login para continuar.", vm.mensagem)
    }
}
