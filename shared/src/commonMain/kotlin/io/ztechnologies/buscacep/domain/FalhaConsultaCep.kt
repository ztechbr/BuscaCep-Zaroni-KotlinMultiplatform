package io.ztechnologies.buscacep.domain

/**
 * Falhas esperadas de uma consulta de CEP.
 *
 * O repositório traduz qualquer problema técnico para um destes casos, de
 * modo que a apresentação só precisa escolher a mensagem adequada ao usuário.
 */
sealed class FalhaConsultaCep(mensagem: String, causa: Throwable? = null) : Exception(mensagem, causa) {

    /** O serviço respondeu normalmente, mas o CEP não existe na base. */
    class CepNaoEncontrado : FalhaConsultaCep("CEP não encontrado")

    /** Não foi possível falar com o serviço: sem rede, DNS ou tempo esgotado. */
    class SemConexao(causa: Throwable) : FalhaConsultaCep("Falha de comunicação com o serviço", causa)

    /** O serviço respondeu com status de erro ou com conteúdo que não reconhecemos. */
    class RespostaInesperada(causa: Throwable? = null) : FalhaConsultaCep("Resposta inesperada do serviço", causa)
}
