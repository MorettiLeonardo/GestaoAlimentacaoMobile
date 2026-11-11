package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.WeightDao
import com.example.gestaoalimentacao.data.model.WeightEntry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WeightViewModelTest {

    private lateinit var app: Application
    private lateinit var dao: WeightDao
    private lateinit var vm: WeightViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        apagarBanco(app)
        SessaoUsuario.iniciar(USUARIO)
        dao = WeightDao(app)
        vm = WeightViewModel(app)
    }

    @After
    fun tearDown() {
        SessaoUsuario.encerrar()
        apagarBanco(app)
    }

    private fun aguardar() = aguardarOperacao({ vm.carregando })

    private fun salvar(peso: String = "72.5", data: String = "10/11/2026", foto: String = "") {
        vm.salvar(peso, data, foto)
        aguardar()
    }

    private fun assertErroSemPersistir(mensagemEsperada: String) {
        assertEquals(mensagemEsperada, vm.mensagem)
        assertFalse(vm.salvo)
        assertTrue(dao.listar(USUARIO).isEmpty())
    }

    // ---------- Estado inicial ----------

    @Test
    fun estadoInicial_estaVazio() {
        assertTrue(vm.registros.isEmpty())
        assertNull(vm.registro)
        assertFalse(vm.carregando)
        assertFalse(vm.salvo)
        assertNull(vm.mensagem)
    }

    // ---------- Sessão ----------

    @Test
    fun semUsuarioLogado_exibeMensagemDeLogin() {
        SessaoUsuario.encerrar()

        vm.listar()
        aguardar()

        assertEquals("Faça login para continuar.", vm.mensagem)
        assertTrue(vm.registros.isEmpty())
    }

    @Test
    fun semUsuarioLogado_naoSalva() {
        SessaoUsuario.encerrar()

        salvar()

        assertEquals("Faça login para continuar.", vm.mensagem)
        assertFalse(vm.salvo)
        assertTrue(dao.listar(USUARIO).isEmpty())
    }

    // ---------- Salvar: sucesso ----------

    @Test
    fun salvar_dadosValidos_cadastra() {
        salvar(peso = " 80.3 ", data = " 01/02/2026 ", foto = "content://media/foto/1")

        assertTrue(vm.salvo)
        assertNull(vm.mensagem)
        assertFalse(vm.carregando)
        val salvo = dao.listar(USUARIO).single()
        assertEquals(80.3, salvo.weightKg, 0.0001)
        assertEquals("01/02/2026", salvo.dateText)
        assertEquals("content://media/foto/1", salvo.photoPath)
    }

    @Test
    fun salvar_aceitaVirgulaComoSeparadorDecimal() {
        salvar(peso = "72,5")
        assertTrue(vm.salvo)
        assertEquals(72.5, dao.listar(USUARIO).single().weightKg, 0.0001)
    }

    @Test
    fun salvar_aceitaPesoInteiro() {
        salvar(peso = "70")
        assertTrue(vm.salvo)
        assertEquals(70.0, dao.listar(USUARIO).single().weightKg, 0.0001)
    }

    @Test
    fun salvar_semFoto_eAceito() {
        salvar(foto = "")
        assertTrue(vm.salvo)
        assertEquals("", dao.listar(USUARIO).single().photoPath)
    }

    // ---------- Salvar: peso ----------

    @Test
    fun salvar_pesoNaoNumerico_mostraErro() {
        salvar(peso = "abc")
        assertErroSemPersistir("Informe o peso em kg.")
    }

    @Test
    fun salvar_pesoVazio_mostraErro() {
        salvar(peso = "  ")
        assertErroSemPersistir("Informe o peso em kg.")
    }

    @Test
    fun salvar_pesoComDoisSeparadores_mostraErro() {
        salvar(peso = "72,5,1")
        assertErroSemPersistir("Informe o peso em kg.")
    }

    @Test
    fun salvar_pesoAbaixoDoMinimo_mostraErroDeFaixa() {
        salvar(peso = "19.9")
        assertErroSemPersistir("O peso deve ficar entre 20 e 500 kg.")
    }

    @Test
    fun salvar_pesoAcimaDoMaximo_mostraErroDeFaixa() {
        salvar(peso = "500.1")
        assertErroSemPersistir("O peso deve ficar entre 20 e 500 kg.")
    }

    @Test
    fun salvar_pesoNegativo_mostraErroDeFaixa() {
        salvar(peso = "-70")
        assertErroSemPersistir("O peso deve ficar entre 20 e 500 kg.")
    }

    @Test
    fun salvar_pesoNosLimites_eAceito() {
        salvar(peso = "20")
        assertTrue(vm.salvo)
        vm.consumirSalvo()

        salvar(peso = "500")
        assertTrue(vm.salvo)

        assertEquals(listOf(500.0, 20.0), dao.listar(USUARIO).map { it.weightKg })
    }

    // ---------- Salvar: data ----------

    @Test
    fun salvar_datasForaDoFormato_mostramErro() {
        val invalidas = listOf("", "1/1/2026", "2026-11-10", "10/11/26", "10-11-2026", "ab/cd/efgh", "10/11/2026 08:00")
        for (data in invalidas) {
            salvar(data = data)
            assertEquals("Data \"$data\" deveria ser rejeitada",
                "Informe a data no formato dd/MM/aaaa.", vm.mensagem)
            assertFalse(vm.salvo)
        }
        assertTrue(dao.listar(USUARIO).isEmpty())
    }

    @Test
    fun salvar_dataValida_eAceitaComTrim() {
        salvar(data = "  31/12/2025  ")
        assertTrue(vm.salvo)
        assertEquals("31/12/2025", dao.listar(USUARIO).single().dateText)
    }

    // ---------- Fluxo de "salvo" ----------

    @Test
    fun aposSalvar_novasOperacoesFicamBloqueadasAteConsumirSalvo() {
        salvar()
        assertTrue(vm.salvo)

        vm.listar()
        aguardar()
        assertTrue("listar não deveria rodar enquanto salvo == true", vm.registros.isEmpty())

        vm.consumirSalvo()
        vm.listar()
        aguardar()
        assertEquals(1, vm.registros.size)
    }

    @Test
    fun mensagemDeErro_eLimpaNaProximaOperacao() {
        salvar(peso = "abc")
        assertEquals("Informe o peso em kg.", vm.mensagem)

        salvar()
        assertNull(vm.mensagem)
        assertTrue(vm.salvo)
    }

    // ---------- Listar ----------

    @Test
    fun listar_semRegistros_retornaVazio() {
        vm.listar()
        aguardar()
        assertTrue(vm.registros.isEmpty())
        assertNull(vm.mensagem)
    }

    @Test
    fun listar_retornaMaisRecentesPrimeiro() {
        dao.cadastrar(USUARIO, WeightEntry(id = "a", weightKg = 80.0, dateText = "01/01/2026"))
        dao.cadastrar(USUARIO, WeightEntry(id = "b", weightKg = 79.0, dateText = "08/01/2026"))
        dao.cadastrar(USUARIO, WeightEntry(id = "c", weightKg = 78.0, dateText = "15/01/2026"))

        vm.listar()
        aguardar()

        assertEquals(listOf("c", "b", "a"), vm.registros.map { it.id })
    }

    @Test
    fun listar_mostraApenasRegistrosDoUsuarioLogado() {
        dao.cadastrar(USUARIO, WeightEntry(id = "meu", weightKg = 70.0))
        dao.cadastrar(OUTRO_USUARIO, WeightEntry(id = "alheio", weightKg = 90.0))

        vm.listar()
        aguardar()

        assertEquals(listOf("meu"), vm.registros.map { it.id })
    }

    // ---------- Buscar por id ----------

    @Test
    fun buscarPorId_existente_carregaRegistro() {
        val original = WeightEntry(id = "p1", weightKg = 68.4, dateText = "05/05/2026", photoPath = "foto.jpg")
        dao.cadastrar(USUARIO, original)

        vm.buscarPorId("p1")
        aguardar()

        assertEquals(original, vm.registro)
        assertNull(vm.mensagem)
    }

    @Test
    fun buscarPorId_inexistente_mostraErro() {
        vm.buscarPorId("nao-existe")
        aguardar()

        assertNull(vm.registro)
        assertEquals("Registro não encontrado.", vm.mensagem)
    }

    @Test
    fun buscarPorId_deOutroUsuario_naoEncontra() {
        dao.cadastrar(OUTRO_USUARIO, WeightEntry(id = "alheio", weightKg = 90.0))

        vm.buscarPorId("alheio")
        aguardar()

        assertNull(vm.registro)
        assertEquals("Registro não encontrado.", vm.mensagem)
    }

    // ---------- Editar ----------

    @Test
    fun salvar_aposBuscarPorId_alteraORegistroExistente() {
        dao.cadastrar(USUARIO, WeightEntry(id = "e1", weightKg = 80.0, dateText = "01/01/2026", photoPath = "velha.jpg"))
        vm.buscarPorId("e1")
        aguardar()

        salvar(peso = "77,2", data = "01/03/2026", foto = "nova.jpg")

        assertTrue(vm.salvo)
        val salvo = dao.listar(USUARIO).single()
        assertEquals("e1", salvo.id)
        assertEquals(77.2, salvo.weightKg, 0.0001)
        assertEquals("01/03/2026", salvo.dateText)
        assertEquals("nova.jpg", salvo.photoPath)
    }

    @Test
    fun salvar_edicaoInvalida_naoAlteraRegistro() {
        val original = WeightEntry(id = "e1", weightKg = 80.0, dateText = "01/01/2026")
        dao.cadastrar(USUARIO, original)
        vm.buscarPorId("e1")
        aguardar()

        salvar(peso = "600")

        assertEquals("O peso deve ficar entre 20 e 500 kg.", vm.mensagem)
        assertEquals(original, dao.buscarPorId(USUARIO, "e1"))
    }

    @Test
    fun novoRegistro_aposBuscarPorId_fazSalvarCriarNovoRegistro() {
        dao.cadastrar(USUARIO, WeightEntry(id = "e1", weightKg = 80.0))
        vm.buscarPorId("e1")
        aguardar()

        vm.novoRegistro()
        assertNull(vm.registro)
        salvar(peso = "75")

        assertEquals(2, dao.listar(USUARIO).size)
        assertEquals(80.0, dao.buscarPorId(USUARIO, "e1")!!.weightKg, 0.0001)
    }

    // ---------- Excluir ----------

    @Test
    fun excluir_removeERecarregaLista() {
        dao.cadastrar(USUARIO, WeightEntry(id = "a", weightKg = 80.0))
        dao.cadastrar(USUARIO, WeightEntry(id = "b", weightKg = 79.0))

        vm.excluir("a")
        aguardar()

        assertNull(vm.mensagem)
        assertEquals(listOf("b"), vm.registros.map { it.id })
        assertNull(dao.buscarPorId(USUARIO, "a"))
    }

    @Test
    fun excluir_inexistente_mostraErro() {
        vm.excluir("nao-existe")
        aguardar()
        assertEquals("Registro de peso não pode ser excluído", vm.mensagem)
    }

    @Test
    fun excluir_registroDeOutroUsuario_naoRemove() {
        dao.cadastrar(OUTRO_USUARIO, WeightEntry(id = "alheio", weightKg = 90.0))

        vm.excluir("alheio")
        aguardar()

        assertEquals("Registro de peso não pode ser excluído", vm.mensagem)
        assertNotNull(dao.buscarPorId(OUTRO_USUARIO, "alheio"))
    }

    private companion object {
        const val USUARIO = 1L
        const val OUTRO_USUARIO = 2L
    }
}
