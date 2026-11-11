package com.example.gestaoalimentacao.ui.viewmodel

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.gestaoalimentacao.auth.AuthManager
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.validation.CadastroValidator
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuthViewModelTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        apagarBanco(app)
        SessaoUsuario.encerrar()
    }

    @After
    fun tearDown() {
        SessaoUsuario.encerrar()
        apagarBanco(app)
    }

    // PBKDF2 com 100 mil iterações é lento no Robolectric: timeout maior.
    private fun aguardar(vm: AuthViewModel) = aguardarOperacao({ vm.carregando }, 20_000)

    private fun cadastrar(
        nome: String = "Maria Silva",
        email: String = "maria@email.com",
        senha: String = "segredo1"
    ): AuthViewModel {
        val vm = AuthViewModel(app)
        vm.cadastrar(nome, email, senha)
        aguardar(vm)
        return vm
    }

    private fun login(email: String, senha: String): AuthViewModel {
        val vm = AuthViewModel(app)
        vm.login(email, senha)
        aguardar(vm)
        return vm
    }

    // ---------- Cadastro ----------

    @Test
    fun cadastro_valido_iniciaSessao() {
        val vm = cadastrar()
        assertNull(vm.mensagem)
        assertTrue(vm.concluido)
        assertNotNull(SessaoUsuario.usuarioId)
    }

    @Test
    fun cadastro_emailDuplicado_ignorandoMaiusculasEEspacos_mostraMensagem() {
        cadastrar()
        SessaoUsuario.encerrar()

        val vm = cadastrar(email = "  MARIA@email.com ")

        assertEquals("Este e-mail já está cadastrado.", vm.mensagem)
        assertFalse(vm.concluido)
        assertNull(SessaoUsuario.usuarioId)
    }

    @Test
    fun cadastro_dadosInvalidos_mostramMensagens() {
        assertEquals("Informe um e-mail válido.", cadastrar(email = "invalido").mensagem)
        assertEquals("Informe um nome entre 3 e 100 caracteres.", cadastrar(nome = "Al").mensagem)
        assertEquals("Use uma senha entre 6 e 128 caracteres.", cadastrar(senha = "123").mensagem)
        assertNull(SessaoUsuario.usuarioId)
    }

    // ---------- Login ----------

    @Test
    fun login_correto_aceitaEmailComMaiusculasEEspacos() {
        cadastrar()
        SessaoUsuario.encerrar()

        val vm = login(" Maria@Email.com ", "segredo1")

        assertTrue(vm.concluido)
        assertNotNull(SessaoUsuario.usuarioId)
    }

    @Test
    fun login_senhaErrada_falha() {
        cadastrar()
        SessaoUsuario.encerrar()

        val vm = login("maria@email.com", "errada99")

        assertEquals("E-mail ou senha incorretos.", vm.mensagem)
        assertFalse(vm.concluido)
        assertNull(SessaoUsuario.usuarioId)
    }

    @Test
    fun login_usuarioInexistente_falhaComMesmaMensagem() {
        val vm = login("ninguem@email.com", "segredo1")
        assertEquals("E-mail ou senha incorretos.", vm.mensagem)
        assertNull(SessaoUsuario.usuarioId)
    }

    @Test
    fun login_senhaCurta_falhaSemConsultarBanco() {
        val vm = login("maria@email.com", "123")
        assertEquals("E-mail ou senha incorretos.", vm.mensagem)
    }

    // ---------- Perfil ----------

    @Test
    fun carregarUsuario_trazDadosEMetaPadrao() {
        cadastrar()
        val vm = AuthViewModel(app)
        vm.carregarUsuario()

        val limite = System.currentTimeMillis() + 5_000
        while (vm.usuario == null && System.currentTimeMillis() < limite) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }

        assertEquals("maria@email.com", vm.usuario?.email)
        assertEquals("Maria Silva", vm.usuario?.name)
        assertEquals(2000, vm.usuario?.calorieGoal)
    }

    // ---------- AuthManager / validador ----------

    @Test
    fun authManager_validaSomenteSenhaCorreta() {
        val salt = AuthManager.criarSalt()
        val hash = AuthManager.gerarHash("abc123", salt)

        assertEquals(32, hash.size)
        assertTrue(AuthManager.validarSenha("abc123", salt, hash))
        assertFalse(AuthManager.validarSenha("abc124", salt, hash))
        assertFalse(AuthManager.validarSenha("abc123", AuthManager.criarSalt(), hash))
    }

    @Test
    fun validador_rejeitaSenhaSoDeEspacos() {
        try {
            CadastroValidator.validar("Maria", "a@b.com", "      ")
            fail("Senha só de espaços deveria ser rejeitada")
        } catch (e: IllegalArgumentException) {
            assertEquals("Use uma senha entre 6 e 128 caracteres.", e.message)
        }
    }
}
