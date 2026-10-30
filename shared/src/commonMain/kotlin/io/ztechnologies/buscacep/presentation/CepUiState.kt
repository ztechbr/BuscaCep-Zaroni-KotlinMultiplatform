package io.ztechnologies.buscacep.presentation

import io.ztechnologies.buscacep.domain.Cep
import io.ztechnologies.buscacep.domain.ValidadorCep

/**
 * Estado imutável da tela de busca.
 *
 * @property cep oito dígitos no máximo, já normalizados e sem máscara.
 * @property resultado endereço da última consulta bem-sucedida; `null`
 * antes da primeira busca, durante uma busca e depois de uma falha.
 */
data class CepUiState(
    val cep: String = "",
    val carregando: Boolean = false,
    val resultado: Cep? = null,
) {
    val cepValido: Boolean get() = ValidadorCep.ehValido(cep)

    val campoHabilitado: Boolean get() = !carregando

    val botaoHabilitado: Boolean get() = cepValido && !carregando
}
