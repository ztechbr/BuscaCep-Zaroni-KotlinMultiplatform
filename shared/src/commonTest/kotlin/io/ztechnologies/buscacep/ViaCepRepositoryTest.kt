package io.ztechnologies.buscacep

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ztechnologies.buscacep.data.ViaCepRepository
import io.ztechnologies.buscacep.data.configurarParaViaCep
import io.ztechnologies.buscacep.domain.Cep
import io.ztechnologies.buscacep.domain.FalhaConsultaCep
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ViaCepRepositoryTest {

    private fun repositorio(tratador: MockRequestHandler) =
        ViaCepRepository(HttpClient(MockEngine(tratador)) { configurarParaViaCep() })

    private val cabecalhoJson = headersOf(HttpHeaders.ContentType, "application/json; charset=utf-8")

    private fun respostaJson(corpo: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
        respond(corpo, status, cabecalhoJson)
    }

    @Test
    fun respostaValidaEhConvertidaParaDominioIgnorandoCamposExtras() = runTest {
        var urlChamada = ""
        val repo = repositorio { requisicao ->
            urlChamada = requisicao.url.toString()
            respond(
                """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "complemento": "lado ímpar",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "ibge": "3550308",
                  "ddd": "11"
                }
                """.trimIndent(),
                HttpStatusCode.OK,
                cabecalhoJson,
            )
        }

        val endereco = repo.buscarCep("01001000")

        assertEquals(Cep("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP"), endereco)
        assertEquals("https://viacep.com.br/ws/01001000/json/", urlChamada)
    }

    @Test
    fun camposAusentesViramTextoVazio() = runTest {
        val repo = repositorio(respostaJson("""{"cep": "01001-000", "localidade": "São Paulo", "uf": "SP"}"""))

        val endereco = repo.buscarCep("01001000")

        assertEquals("", endereco.logradouro)
        assertEquals("", endereco.bairro)
    }

    @Test
    fun erroBooleanoIndicaCepInexistente() = runTest {
        val repo = repositorio(respostaJson("""{"erro": true}"""))
        assertFailsWith<FalhaConsultaCep.CepNaoEncontrado> { repo.buscarCep("99999999") }
    }

    @Test
    fun erroComoTextoIndicaCepInexistente() = runTest {
        val repo = repositorio(respostaJson("""{"erro": "true"}"""))
        assertFailsWith<FalhaConsultaCep.CepNaoEncontrado> { repo.buscarCep("99999999") }
    }

    @Test
    fun statusDeErroDoServidorEhRespostaInesperada() = runTest {
        val repo = repositorio(respostaJson("", HttpStatusCode.InternalServerError))
        assertFailsWith<FalhaConsultaCep.RespostaInesperada> { repo.buscarCep("01001000") }
    }

    @Test
    fun corpoQueNaoEhJsonEhRespostaInesperada() = runTest {
        val repo = repositorio(respostaJson("<html>manutenção</html>"))
        assertFailsWith<FalhaConsultaCep.RespostaInesperada> { repo.buscarCep("01001000") }
    }

    @Test
    fun falhaDeRedeEhSemConexao() = runTest {
        val repo = repositorio { throw IOException("rede indisponível") }
        assertFailsWith<FalhaConsultaCep.SemConexao> { repo.buscarCep("01001000") }
    }
}
