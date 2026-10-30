package io.ztechnologies.buscacep.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ztechnologies.buscacep.domain.Cep
import io.ztechnologies.buscacep.domain.FalhaConsultaCep
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Implementação de [CepRepository] sobre a API pública do ViaCEP.
 *
 * O [HttpClient] é criado uma única vez e reaproveitado entre as consultas;
 * quem cria o repositório deve chamar [close] ao final do ciclo de vida.
 * O engine HTTP é escolhido pelo Ktor conforme a plataforma (OkHttp no
 * Android, CIO no Desktop e Darwin no iOS).
 */
class ViaCepRepository(
    private val httpClient: HttpClient = criarHttpClientViaCep(),
) : CepRepository, AutoCloseable {

    override suspend fun buscarCep(cep: String): Cep {
        val resposta = requisitar(cep)
        if (!resposta.status.isSuccess()) throw FalhaConsultaCep.RespostaInesperada()

        val dto = lerCorpo(resposta)
        if (dto.indicaCepInexistente) throw FalhaConsultaCep.CepNaoEncontrado()
        if (dto.cep.isBlank()) throw FalhaConsultaCep.RespostaInesperada()

        return dto.paraDominio()
    }

    override fun close() = httpClient.close()

    /**
     * Qualquer exceção durante o envio é tratada como problema de conexão.
     * Os engines lançam tipos diferentes para a mesma situação (por exemplo,
     * `UnresolvedAddressException` na JVM não é uma `IOException`), por isso
     * a captura é ampla, mas o cancelamento continua sendo propagado.
     */
    private suspend fun requisitar(cep: String): HttpResponse =
        try {
            httpClient.get("$URL_BASE/$cep/json/")
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (falha: Exception) {
            throw FalhaConsultaCep.SemConexao(falha)
        }

    private suspend fun lerCorpo(resposta: HttpResponse): ViaCepDto =
        try {
            resposta.body()
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (falha: Exception) {
            throw FalhaConsultaCep.RespostaInesperada(falha)
        }

    private companion object {
        const val URL_BASE = "https://viacep.com.br/ws"
    }
}

private const val TEMPO_LIMITE_CONEXAO_MS = 10_000L
private const val TEMPO_LIMITE_REQUISICAO_MS = 15_000L

/**
 * Plugins usados pelo cliente do [ViaCepRepository]. Fica separado da
 * criação do cliente para que os testes apliquem a mesma configuração
 * sobre um `MockEngine`.
 */
internal fun HttpClientConfig<*>.configurarParaViaCep() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = TEMPO_LIMITE_CONEXAO_MS
        requestTimeoutMillis = TEMPO_LIMITE_REQUISICAO_MS
    }
}

fun criarHttpClientViaCep(): HttpClient = HttpClient { configurarParaViaCep() }
