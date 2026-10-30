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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import io.ztechnologies.buscacep.presentation.TemaBuscaCep

/**
 * Ponto de entrada da interface compartilhada por Android, Desktop e iOS.
 */
@Composable
@Preview
fun App() {
    TemaBuscaCep {
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
            color = MaterialTheme.colorScheme.onSurface,
        )

        OutlinedTextField(
            value = estado.cep,
            onValueChange = aoAlterarCep,
            enabled = estado.campoHabilitado,
            label = { Text("Digite o CEP") },
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

        // Largura total, como no leiaute do enunciado: área de toque maior e
        // alinhamento com o campo de texto logo acima.
        ElevatedButton(
            onClick = aoBuscar,
            enabled = estado.botaoHabilitado,
            modifier = Modifier.widthIn(max = LARGURA_MAXIMA_CONTEUDO).fillMaxWidth(),
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
    val corRotulo = MaterialTheme.colorScheme.onSurfaceVariant
    val corValor = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier.widthIn(max = LARGURA_MAXIMA_CONTEUDO).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(linhaResultado("CEP", endereco?.cep, corRotulo, corValor))
        Text(linhaResultado("Logradouro", endereco?.logradouro, corRotulo, corValor))
        Text(linhaResultado("Bairro", endereco?.bairro, corRotulo, corValor))
        Text(linhaResultado("Localidade", endereco?.localidade, corRotulo, corValor))
        Text(linhaResultado("UF", endereco?.uf, corRotulo, corValor))
    }
}

/**
 * Rótulo em cor secundária e valor em destaque, para que o dado retornado
 * seja o primeiro elemento percebido na linha.
 */
private fun linhaResultado(rotulo: String, valor: String?, corRotulo: Color, corValor: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = corRotulo)) { append("$rotulo: ") }
        withStyle(SpanStyle(color = corValor, fontWeight = FontWeight.Medium)) { append(valor.orEmpty()) }
    }

private val LARGURA_MAXIMA_CONTEUDO = 480.dp
