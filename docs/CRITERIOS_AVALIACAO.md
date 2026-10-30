# Critérios de avaliação

Correspondência entre os requisitos do enunciado e o código. Os caminhos são relativos a `shared/src/commonMain/kotlin/io/ztechnologies/buscacep/`, exceto quando indicado.

| Item | Requisito | Onde está implementado | Teste automatizado |
| --- | --- | --- | --- |
| R01 | Cliente HTTP multiplataforma (Ktor) consultando o ViaCEP | `data/ViaCepRepository.kt`, `data/ViaCepDto.kt`; engines em `shared/build.gradle.kts` (OkHttp, CIO, Darwin) | `ViaCepRepositoryTest` |
| R02 | `OutlinedTextField` para o CEP | `App.kt` → `TelaBuscaCep` | — |
| R03 | `ElevatedButton` desabilitado com CEP inválido | `App.kt` (`enabled = estado.botaoHabilitado`); regra em `presentation/CepUiState.kt` | `CepValidationTest` |
| R04 | Cinco `Text` de resultado | `App.kt` → `ResultadoBusca` | — |
| R05 | `ViewModel` gerenciando estado e chamada à API | `presentation/CepViewModel.kt` | `CepViewModelTest` |
| R06 | Limpar os cinco textos ao iniciar a consulta | `CepViewModel.buscar()` (`resultado = null`) | `novaConsultaLimpaResultadoAnteriorAntesDaResposta` |
| R07 | Campo desabilitado durante a consulta | `CepUiState.campoHabilitado`; `App.kt` (`enabled`) | `duranteConsultaControlesFicamDesabilitados` |
| R08 | Botão desabilitado e `CircularProgressIndicator` dentro dele | `App.kt` → conteúdo do `ElevatedButton` | `duranteConsultaControlesFicamDesabilitados` |
| R09 | Exibir os cinco campos no sucesso | `CepViewModel.buscar()`; `App.kt` → `ResultadoBusca` | `sucessoPreencheOsCincoCamposEReabilitaControles` |
| R10 | Reabilitar o campo ao concluir | `CepViewModel.buscar()` (`carregando = false` no sucesso e na falha) | `sucessoPreencheOsCincoCamposEReabilitaControles`, `cepInexistenteGeraMensagemERestabeleceControles` |
| R11 | Reabilitar o botão e restaurar o texto | `CepUiState.botaoHabilitado`; `App.kt` (texto "Buscar" quando não está carregando) | mesmos de R10 |
| R12 | Informar erro por `Snackbar` | `CepViewModel.mensagensErro`; `App.kt` (`SnackbarHost` + `LaunchedEffect`); textos em `presentation/MensagensErro.kt` | `cepInexistenteGeraMensagemERestabeleceControles`, `falhaDeRedeNaoDeixaEstadoPresoEmCarregamento` |

Os itens de interface (R02, R04, R08 e a exibição da Snackbar) não têm teste de UI automatizado; a verificação deles é feita executando o aplicativo.
