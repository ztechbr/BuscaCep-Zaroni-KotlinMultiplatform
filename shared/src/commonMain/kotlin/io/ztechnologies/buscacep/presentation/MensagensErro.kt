package io.ztechnologies.buscacep.presentation

import io.ztechnologies.buscacep.domain.FalhaConsultaCep

/**
 * Textos exibidos ao usuário quando algo dá errado. Ficam reunidos aqui
 * para que nenhum detalhe técnico (stack trace, URL, código HTTP) chegue
 * à Snackbar.
 */
object MensagensErro {

    const val CEP_INVALIDO = "Informe um CEP com oito dígitos."
    const val CEP_NAO_ENCONTRADO = "Não encontramos esse CEP. Confira os números e tente novamente."
    const val SEM_CONEXAO = "Não foi possível consultar o CEP agora. Verifique sua conexão e tente novamente."
    const val RESPOSTA_INESPERADA = "O serviço de CEP respondeu de forma inesperada. Tente novamente em instantes."

    fun para(falha: FalhaConsultaCep): String = when (falha) {
        is FalhaConsultaCep.CepNaoEncontrado -> CEP_NAO_ENCONTRADO
        is FalhaConsultaCep.SemConexao -> SEM_CONEXAO
        is FalhaConsultaCep.RespostaInesperada -> RESPOSTA_INESPERADA
    }
}
