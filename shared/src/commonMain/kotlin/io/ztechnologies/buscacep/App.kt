package io.ztechnologies.buscacep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ztechnologies.buscacep.data.ViaCepRepository
import io.ztechnologies.buscacep.presentation.CepUiState
import io.ztechnologies.buscacep.presentation.CepViewModel
import io.ztechnologies.buscacep.presentation.MascaraCep

/**
 * Ponto de entrada da interface compartilhada por Android, Desktop e iOS.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = viewModel {
            val repositorio = ViaCepRepository()
            // O HttpClient vive enquanto a ViewModel viver.
            CepViewModel(repositorio).apply { addCloseable(repositorio) }
        }
        val estado by viewModel.estado.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel) {
            viewModel.mensagensErro.collect { mensagem ->
                snackbarHostState.showSnackbar(mensagem)
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { espacamentoInterno ->
            TelaBuscaCep(
                estado = estado,
                aoAlterarCep = viewModel::aoAlterarCep,
                aoBuscar = viewModel::buscar,
                modifier = Modifier.padding(espacamentoInterno),
            )
        }
    }
}

@Composable
private fun TelaBuscaCep(
    estado: CepUiState,
    aoAlterarCep: (String) -> Unit,
    aoBuscar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Busca de CEP",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = estado.cep,
            onValueChange = aoAlterarCep,
            enabled = estado.campoHabilitado,
            label = { Text("CEP") },
            placeholder = { Text("00000-000") },
            singleLine = true,
            visualTransformation = MascaraCep,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { if (estado.botaoHabilitado) aoBuscar() }),
            modifier = Modifier.widthIn(max = LARGURA_MAXIMA_CONTEUDO).fillMaxWidth(),
        )

        ElevatedButton(
            onClick = aoBuscar,
            enabled = estado.botaoHabilitado,
        ) {
            if (estado.carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Buscar")
            }
        }

        ResultadoBusca(estado)
    }
}

/**
 * Os cinco textos de resultado ficam sempre visíveis; antes da busca e
 * durante o carregamento, apenas os rótulos aparecem.
 */
@Composable
private fun ResultadoBusca(estado: CepUiState) {
    val endereco = estado.resultado
    Column(
        modifier = Modifier.widthIn(max = LARGURA_MAXIMA_CONTEUDO).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("CEP: ${endereco?.cep.orEmpty()}")
        Text("Logradouro: ${endereco?.logradouro.orEmpty()}")
        Text("Bairro: ${endereco?.bairro.orEmpty()}")
        Text("Localidade: ${endereco?.localidade.orEmpty()}")
        Text("UF: ${endereco?.uf.orEmpty()}")
    }
}

private val LARGURA_MAXIMA_CONTEUDO = 480.dp
