package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.data.dal.WaterDao
import com.example.gestaoalimentacao.data.model.WaterIntake
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WaterViewModelTest {

    private lateinit var app: Application
    private lateinit var dao: WaterDao
    private lateinit var vm: WaterViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        apagarBanco(app)
        SessaoUsuario.iniciar(USUARIO)
        dao = WaterDao(app)
        vm = WaterViewModel(app)
    }

    @After
    fun tearDown() {
        SessaoUsuario.encerrar()
        apagarBanco(app)
    }

    private fun aguardar() = aguardarOperacao({ vm.carregando })

    private fun salvar(quantidade: String = "250", horario: String = "08:30", anotacao: String = "") {
        vm.salvar(quantidade, horario, anotacao)
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
        assertEquals(0, vm.totalMl)
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
    fun salvar_dadosValidos_cadastraComValoresAparados() {
        salvar(quantidade = "  300 ", horario = " 07:45 ", anotacao = "  Após o treino  ")

        assertTrue(vm.salvo)
        assertNull(vm.mensagem)
        assertFalse(vm.carregando)
        val salvos = dao.listar(USUARIO)
        assertEquals(1, salvos.size)
        assertEquals(300, salvos[0].amountMl)
        assertEquals("07:45", salvos[0].timeText)
        assertEquals("Após o treino", salvos[0].note)
    }

    @Test
    fun salvar_anotacaoVaziaEhPermitida() {
        salvar(anotacao = "")
        assertTrue(vm.salvo)
        assertEquals("", dao.listar(USUARIO).single().note)
    }

    // ---------- Salvar: quantidade ----------

    @Test
    fun salvar_quantidadeNaoNumerica_mostraErro() {
        salvar(quantidade = "abc")
        assertErroSemPersistir("Informe a quantidade em ml.")
    }

    @Test
    fun salvar_quantidadeVazia_mostraErro() {
        salvar(quantidade = "   ")
        assertErroSemPersistir("Informe a quantidade em ml.")
    }

    @Test
    fun salvar_quantidadeDecimal_mostraErro() {
        salvar(quantidade = "250.5")
        assertErroSemPersistir("Informe a quantidade em ml.")
    }

    @Test
    fun salvar_quantidadeZero_mostraErroDeFaixa() {
        salvar(quantidade = "0")
        assertErroSemPersistir("A quantidade deve ficar entre 1 e 5000 ml.")
    }

    @Test
    fun salvar_quantidadeNegativa_mostraErroDeFaixa() {
        salvar(quantidade = "-10")
        assertErroSemPersistir("A quantidade deve ficar entre 1 e 5000 ml.")
    }

    @Test
    fun salvar_quantidadeAcimaDoLimite_mostraErroDeFaixa() {
        salvar(quantidade = "5001")
        assertErroSemPersistir("A quantidade deve ficar entre 1 e 5000 ml.")
    }

    @Test
    fun salvar_quantidadeNosLimites_eAceita() {
        salvar(quantidade = "1")
        assertTrue(vm.salvo)
        vm.consumirSalvo()

        salvar(quantidade = "5000")
        assertTrue(vm.salvo)

        assertEquals(listOf(5000, 1), dao.listar(USUARIO).map { it.amountMl })
    }

    // ---------- Salvar: horário ----------

    @Test
    fun salvar_horariosInvalidos_mostramErro() {
        val invalidos = listOf("", "7:30", "24:00", "12:60", "12-30", "1230", "ab:cd", "12:30:00")
        for (horario in invalidos) {
            salvar(horario = horario)
            assertEquals("Horário \"$horario\" deveria ser rejeitado",
                "Informe o horário no formato HH:mm.", vm.mensagem)
            assertFalse(vm.salvo)
        }
        assertTrue(dao.listar(USUARIO).isEmpty())
    }

    @Test
    fun salvar_horariosValidos_saoAceitos() {
        for (horario in listOf("00:00", "09:05", "19:59", "23:59")) {
            salvar(horario = horario)
            assertTrue("Horário \"$horario\" deveria ser aceito", vm.salvo)
            vm.consumirSalvo()
        }
        assertEquals(4, dao.listar(USUARIO).size)
    }

    // ---------- Salvar: anotação ----------

    @Test
    fun salvar_anotacaoCom101Caracteres_mostraErro() {
        salvar(anotacao = "a".repeat(101))
        assertErroSemPersistir("A anotação pode ter até 100 caracteres.")
    }

    @Test
    fun salvar_anotacaoCom100Caracteres_eAceita() {
        salvar(anotacao = "a".repeat(100))
        assertTrue(vm.salvo)
    }

    @Test
    fun salvar_anotacaoLongaSoPorEspacos_eAceitaAposTrim() {
        salvar(anotacao = "  " + "a".repeat(100) + "  ")
        assertTrue(vm.salvo)
        assertEquals(100, dao.listar(USUARIO).single().note.length)
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
        assertFalse(vm.salvo)
        vm.listar()
        aguardar()
        assertEquals(1, vm.registros.size)
    }

    @Test
    fun mensagemDeErro_eLimpaNaProximaOperacao() {
        salvar(quantidade = "abc")
        assertEquals("Informe a quantidade em ml.", vm.mensagem)

        salvar()
        assertNull(vm.mensagem)
        assertTrue(vm.salvo)
    }

    // ---------- Listar ----------

    @Test
    fun listar_semRegistros_retornaVazioETotalZero() {
        vm.listar()
        aguardar()
        assertTrue(vm.registros.isEmpty())
        assertEquals(0, vm.totalMl)
        assertNull(vm.mensagem)
    }

    @Test
    fun listar_retornaMaisRecentesPrimeiroESomaTotal() {
        dao.cadastrar(USUARIO, WaterIntake(id = "a", amountMl = 200, timeText = "08:00"))
        dao.cadastrar(USUARIO, WaterIntake(id = "b", amountMl = 350, timeText = "10:00"))
        dao.cadastrar(USUARIO, WaterIntake(id = "c", amountMl = 500, timeText = "12:00"))

        vm.listar()
        aguardar()

        assertEquals(listOf("c", "b", "a"), vm.registros.map { it.id })
        assertEquals(1050, vm.totalMl)
    }

    @Test
    fun listar_mostraApenasRegistrosDoUsuarioLogado() {
        dao.cadastrar(USUARIO, WaterIntake(id = "meu", amountMl = 200))
        dao.cadastrar(OUTRO_USUARIO, WaterIntake(id = "alheio", amountMl = 900))

        vm.listar()
        aguardar()

        assertEquals(listOf("meu"), vm.registros.map { it.id })
        assertEquals(200, vm.totalMl)
    }

    // ---------- Buscar por id ----------

    @Test
    fun buscarPorId_existente_carregaRegistro() {
        val original = WaterIntake(id = "x1", amountMl = 450, timeText = "15:20", note = "chá")
        dao.cadastrar(USUARIO, original)

        vm.buscarPorId("x1")
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
        dao.cadastrar(OUTRO_USUARIO, WaterIntake(id = "alheio", amountMl = 900))

        vm.buscarPorId("alheio")
        aguardar()

        assertNull(vm.registro)
        assertEquals("Registro não encontrado.", vm.mensagem)
    }

    // ---------- Editar ----------

    @Test
    fun salvar_aposBuscarPorId_alteraORegistroExistente() {
        dao.cadastrar(USUARIO, WaterIntake(id = "e1", amountMl = 200, timeText = "08:00", note = "antes"))
        vm.buscarPorId("e1")
        aguardar()

        salvar(quantidade = "750", horario = "21:10", anotacao = "depois")

        assertTrue(vm.salvo)
        val salvos = dao.listar(USUARIO)
        assertEquals(1, salvos.size)
        assertEquals(WaterIntake(id = "e1", amountMl = 750, timeText = "21:10", note = "depois"), salvos[0])
    }

    @Test
    fun salvar_edicaoInvalida_naoAlteraRegistro() {
        val original = WaterIntake(id = "e1", amountMl = 200, timeText = "08:00", note = "antes")
        dao.cadastrar(USUARIO, original)
        vm.buscarPorId("e1")
        aguardar()

        salvar(quantidade = "9999")

        assertEquals("A quantidade deve ficar entre 1 e 5000 ml.", vm.mensagem)
        assertEquals(original, dao.buscarPorId(USUARIO, "e1"))
    }

    @Test
    fun novoRegistro_aposBuscarPorId_fazSalvarCriarNovoRegistro() {
        dao.cadastrar(USUARIO, WaterIntake(id = "e1", amountMl = 200, timeText = "08:00"))
        vm.buscarPorId("e1")
        aguardar()

        vm.novoRegistro()
        assertNull(vm.registro)
        salvar(quantidade = "100")

        val salvos = dao.listar(USUARIO)
        assertEquals(2, salvos.size)
        assertEquals(200, dao.buscarPorId(USUARIO, "e1")!!.amountMl)
    }

    // ---------- Excluir ----------

    @Test
    fun excluir_removeERecarregaListaETotal() {
        dao.cadastrar(USUARIO, WaterIntake(id = "a", amountMl = 200))
        dao.cadastrar(USUARIO, WaterIntake(id = "b", amountMl = 300))
        vm.listar()
        aguardar()
        assertEquals(500, vm.totalMl)

        vm.excluir("a")
        aguardar()

        assertNull(vm.mensagem)
        assertEquals(listOf("b"), vm.registros.map { it.id })
        assertEquals(300, vm.totalMl)
        assertNull(dao.buscarPorId(USUARIO, "a"))
    }

    @Test
    fun excluir_inexistente_mostraErro() {
        vm.excluir("nao-existe")
        aguardar()
        assertEquals("Registro de água não pode ser excluído", vm.mensagem)
    }

    @Test
    fun excluir_registroDeOutroUsuario_naoRemove() {
        dao.cadastrar(OUTRO_USUARIO, WaterIntake(id = "alheio", amountMl = 900))

        vm.excluir("alheio")
        aguardar()

        assertEquals("Registro de água não pode ser excluído", vm.mensagem)
        assertTrue(dao.buscarPorId(OUTRO_USUARIO, "alheio") != null)
    }

    private companion object {
        const val USUARIO = 1L
        const val OUTRO_USUARIO = 2L
    }
}
