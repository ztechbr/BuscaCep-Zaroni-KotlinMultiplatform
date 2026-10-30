package io.ztechnologies.buscacep

import io.ztechnologies.buscacep.data.CepRepository
import io.ztechnologies.buscacep.domain.Cep
import io.ztechnologies.buscacep.domain.FalhaConsultaCep
import io.ztechnologies.buscacep.presentation.CepViewModel
import io.ztechnologies.buscacep.presentation.MensagensErro
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Repositório controlado pelo teste: cada chamada fica suspensa até que o
 * teste decida a resposta, o que permite inspecionar o estado "durante"
 * a consulta sem depender de atrasos reais.
 */
private class RepositorioControlado : CepRepository {
    val chamadas = mutableListOf<String>()
    private var respostaPendente = CompletableDeferred<Result<Cep>>()

    override suspend fun buscarCep(cep: String): Cep {
        chamadas += cep
        return respostaPendente.await().getOrThrow()
    }

    fun responder(endereco: Cep) {
        respostaPendente.complete(Result.success(endereco))
    }

    fun falhar(falha: FalhaConsultaCep) {
        respostaPendente.complete(Result.failure(falha))
    }

    fun prepararNovaConsulta() {
        respostaPendente = CompletableDeferred()
    }
}

private val PRACA_DA_SE = Cep("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP")
private val AV_PAULISTA = Cep("01310-100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP")

@OptIn(ExperimentalCoroutinesApi::class)
class CepViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repositorio = RepositorioControlado()
    private lateinit var viewModel: CepViewModel

    @BeforeTest
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        viewModel = CepViewModel(repositorio)
    }

    @AfterTest
    fun limpar() {
        Dispatchers.resetMain()
    }

    private fun testar(corpo: suspend TestScope.() -> Unit) = runTest(dispatcher) { corpo() }

    @Test
    fun digitacaoNormalizaEntradaEHabilitaBotaoComOitoDigitos() = testar {
        viewModel.aoAlterarCep("01001-00")
        assertEquals("0100100", viewModel.estado.value.cep)
        assertFalse(viewModel.estado.value.botaoHabilitado)

        viewModel.aoAlterarCep("01001-000")
        assertEquals("01001000", viewModel.estado.value.cep)
        assertTrue(viewModel.estado.value.botaoHabilitado)
    }

    @Test
    fun duranteConsultaControlesFicamDesabilitados() = testar {
        viewModel.aoAlterarCep("01001000")
        viewModel.buscar()
        runCurrent()

        val estado = viewModel.estado.value
        assertTrue(estado.carregando)
        assertFalse(estado.campoHabilitado)
        assertFalse(estado.botaoHabilitado)
        assertEquals("01001000", estado.cep, "O CEP digitado não deve ser apagado")
    }

    @Test
    fun sucessoPreencheOsCincoCamposEReabilitaControles() = testar {
        viewModel.aoAlterarCep("01001000")
        viewModel.buscar()
        runCurrent()
        repositorio.responder(PRACA_DA_SE)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(PRACA_DA_SE, estado.resultado)
        assertFalse(estado.carregando)
        assertTrue(estado.campoHabilitado)
        assertTrue(estado.botaoHabilitado)
        assertEquals(listOf("01001000"), repositorio.chamadas)
    }

    @Test
    fun novaConsultaLimpaResultadoAnteriorAntesDaResposta() = testar {
        viewModel.aoAlterarCep("01001000")
        viewModel.buscar()
        runCurrent()
        repositorio.responder(PRACA_DA_SE)
        advanceUntilIdle()

        repositorio.prepararNovaConsulta()
        viewModel.aoAlterarCep("01310100")
        viewModel.buscar()
        runCurrent()
        assertNull(viewModel.estado.value.resultado)

        repositorio.responder(AV_PAULISTA)
        advanceUntilIdle()
        assertEquals(AV_PAULISTA, viewModel.estado.value.resultado)
    }

    @Test
    fun cepInexistenteGeraMensagemERestabeleceControles() = testar {
        viewModel.aoAlterarCep("99999999")
        viewModel.buscar()
        runCurrent()
        repositorio.falhar(FalhaConsultaCep.CepNaoEncontrado())
        advanceUntilIdle()

        assertEquals(MensagensErro.CEP_NAO_ENCONTRADO, viewModel.mensagensErro.first())
        val estado = viewModel.estado.value
        assertNull(estado.resultado)
        assertFalse(estado.carregando)
        assertTrue(estado.campoHabilitado)
        assertTrue(estado.botaoHabilitado)
    }

    @Test
    fun falhaDeRedeNaoDeixaEstadoPresoEmCarregamento() = testar {
        viewModel.aoAlterarCep("01001000")
        viewModel.buscar()
        runCurrent()
        repositorio.falhar(FalhaConsultaCep.SemConexao(Exception("tempo esgotado")))
        advanceUntilIdle()

        assertEquals(MensagensErro.SEM_CONEXAO, viewModel.mensagensErro.first())
        assertFalse(viewModel.estado.value.carregando)
        assertTrue(viewModel.estado.value.botaoHabilitado)
    }

    @Test
    fun cliquesRepetidosDuranteConsultaNaoDisparamNovasRequisicoes() = testar {
        viewModel.aoAlterarCep("01001000")
        repeat(5) { viewModel.buscar() }
        runCurrent()

        assertEquals(1, repositorio.chamadas.size)

        repositorio.responder(PRACA_DA_SE)
        advanceUntilIdle()
        assertEquals(1, repositorio.chamadas.size)
    }

    @Test
    fun digitacaoDuranteConsultaEhIgnorada() = testar {
        viewModel.aoAlterarCep("01001000")
        viewModel.buscar()
        runCurrent()

        viewModel.aoAlterarCep("1234")
        assertEquals("01001000", viewModel.estado.value.cep)
    }

    @Test
    fun buscarComCepInvalidoNaoConsultaEInformaUsuario() = testar {
        viewModel.aoAlterarCep("123")
        viewModel.buscar()
        advanceUntilIdle()

        assertTrue(repositorio.chamadas.isEmpty())
        assertEquals(MensagensErro.CEP_INVALIDO, viewModel.mensagensErro.first())
    }
}
