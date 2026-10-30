package io.ztechnologies.buscacep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ztechnologies.buscacep.data.CepRepository
import io.ztechnologies.buscacep.domain.FalhaConsultaCep
import io.ztechnologies.buscacep.domain.ValidadorCep
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordena a tela de busca: recebe o texto digitado, dispara a consulta
 * no [CepRepository] e publica o resultado em [estado].
 *
 * Mensagens de erro saem por [mensagensErro], um fluxo de eventos
 * consumidos uma única vez. Diferente de um campo no estado, o evento não
 * reaparece quando a tela é recomposta ou recriada (rotação, por exemplo).
 */
class CepViewModel(
    private val repositorio: CepRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow(CepUiState())
    val estado: StateFlow<CepUiState> = _estado.asStateFlow()

    private val _mensagensErro = Channel<String>(Channel.BUFFERED)
    val mensagensErro: Flow<String> = _mensagensErro.receiveAsFlow()

    fun aoAlterarCep(texto: String) {
        if (_estado.value.carregando) return
        _estado.update { it.copy(cep = ValidadorCep.normalizar(texto)) }
    }

    /**
     * Inicia a consulta do CEP atual.
     *
     * Chamadas feitas enquanto outra consulta está em andamento são
     * ignoradas: como o estado passa a `carregando` de forma síncrona,
     * cliques repetidos não geram requisições duplicadas.
     */
    fun buscar() {
        val estadoAtual = _estado.value
        if (estadoAtual.carregando) return
        if (!estadoAtual.cepValido) {
            _mensagensErro.trySend(MensagensErro.CEP_INVALIDO)
            return
        }

        _estado.update { it.copy(carregando = true, resultado = null) }

        // Se o escopo for cancelado (ViewModel descartada), a CancellationException
        // não é capturada aqui e nenhum estado é publicado depois disso.
        viewModelScope.launch {
            try {
                val endereco = repositorio.buscarCep(estadoAtual.cep)
                _estado.update { it.copy(carregando = false, resultado = endereco) }
            } catch (falha: FalhaConsultaCep) {
                _estado.update { it.copy(carregando = false, resultado = null) }
                _mensagensErro.send(MensagensErro.para(falha))
            }
        }
    }
}
