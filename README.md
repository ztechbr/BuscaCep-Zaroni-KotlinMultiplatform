# BuscaCEP

Aplicativo de busca de CEP feito com **Kotlin Multiplatform** e **Compose Multiplatform**. A mesma interface e a mesma lógica rodam no **Android**, no **Desktop (JVM)** e no **iOS**. Os dados vêm da API pública [ViaCEP](https://viacep.com.br).

Trabalho final da disciplina *Arquiteturas e Padrões Modernos para Desenvolvimento para Dispositivos Móveis (KMP)*.

## Controle de versão

| Versão | Data | Commit | Descrição |
| --- | --- | --- | --- |
| 1.0.0 | 30/10/2026 | `64574c0` | Busca de CEP com ViaCEP para Android, Desktop e iOS |
| 1.1.0 | 30/10/2026 | `PENDENTE` | Tema claro/escuro com contraste WCAG AA, fonte Inter e leiaute alinhado às figuras do enunciado |

A versão do aplicativo também está em `androidApp/build.gradle.kts` (`versionName`), `desktopApp/build.gradle.kts` (`packageVersion`) e `iosApp/Configuration/Config.xcconfig` (`MARKETING_VERSION`). Para conferir o commit da cópia local, use `git log -1 --format="%h %ad" --date=short`.

## Funcionalidades

- Campo `OutlinedTextField` que aceita apenas dígitos (até oito) e exibe a máscara `00000-000`.
- Botão `ElevatedButton` "Buscar", habilitado somente quando há oito dígitos e nenhuma consulta em andamento.
- Durante a consulta: os resultados anteriores são limpos, o campo e o botão ficam desabilitados e um `CircularProgressIndicator` aparece dentro do botão.
- Ao concluir: cinco `Text` exibem CEP, logradouro, bairro, localidade e UF; campo e botão voltam a ficar habilitados.
- Em caso de erro (CEP inexistente, falha de rede, tempo esgotado ou resposta inesperada), uma `Snackbar` informa o usuário.
- Tema claro e escuro que acompanha a preferência do sistema, com tipografia Inter (detalhes em [Interface e acessibilidade](#interface-e-acessibilidade)).

O mapeamento de cada requisito (R01–R12) para o código está em [docs/CRITERIOS_AVALIACAO.md](docs/CRITERIOS_AVALIACAO.md).

## Pré-requisitos

- Android Studio com suporte a Kotlin Multiplatform.
- JDK 17 ou superior para executar o Gradle (o código é compilado com alvo JVM 11).
- Android SDK com a plataforma 37 (`compileSdk`/`targetSdk` 37, `minSdk` 27).
- Para iOS: **macOS com Xcode**. O build nativo iOS não é possível em Windows ou Linux.

Principais versões (definidas em `gradle/libs.versions.toml`): Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.1.1, Gradle 9.5.1, Ktor 3.6.1, kotlinx-coroutines 1.11.0, Lifecycle 2.11.0.

## Estrutura dos módulos

```text
BuscaCEPZaroni/
├── shared/       Módulo KMP com a UI Compose e toda a lógica (Android, JVM e iOS)
├── androidApp/   Aplicativo Android: MainActivity chama App()
├── desktopApp/   Aplicativo Desktop: main() abre uma Window com App()
└── iosApp/       Projeto Xcode: ContentView exibe MainViewController() do framework Shared
```

Código compartilhado em `shared/src/commonMain/kotlin/io/ztechnologies/buscacep/`:

```text
App.kt                          Tela Compose (campo, botão, resultados e Snackbar)
domain/
  Cep.kt                        Modelo de endereço, independente de HTTP
  ValidadorCep.kt               Normalização e validação dos oito dígitos
  FalhaConsultaCep.kt           Tipos de falha: não encontrado, sem conexão, resposta inesperada
data/
  CepRepository.kt              Contrato de consulta (permite usar um fake nos testes)
  ViaCepRepository.kt           Cliente Ktor do ViaCEP, timeouts e tradução de erros
  ViaCepDto.kt                  Formato JSON da resposta do ViaCEP
presentation/
  CepUiState.kt                 Estado imutável da tela
  CepViewModel.kt               Validação, consulta, carregamento e eventos de erro
  MensagensErro.kt              Mensagens exibidas ao usuário
  MascaraCep.kt                 Máscara visual 00000-000
  TemaBuscaCep.kt               Cores (claro/escuro) e tipografia Inter
composeResources/font/          Arquivos da fonte Inter (Regular, Medium e SemiBold)
```

## Arquitetura

```text
Compose (App.kt) → CepViewModel → CepRepository → ViaCepRepository (Ktor) → ViaCEP
```

- **`CepViewModel`** (Lifecycle Multiplatform) expõe o estado como `StateFlow<CepUiState>` e mantém o `MutableStateFlow` privado. Ao buscar, marca `carregando = true` e apaga o resultado anterior de forma síncrona; por isso cliques repetidos não disparam requisições duplicadas.
- **Erros** saem por um `Channel` exposto como `Flow<String>`. Cada mensagem é entregue uma única vez, então a Snackbar não reaparece após recomposição ou rotação de tela.
- **`ViaCepRepository`** usa um único `HttpClient`, criado junto com a ViewModel e fechado em `onCleared` por meio de `addCloseable`. Timeouts: 10 s para conexão e 15 s para a requisição. O `CancellationException` é sempre propagado, nunca convertido em erro de negócio.
- **Engine HTTP por plataforma**: OkHttp no Android, CIO no Desktop e Darwin no iOS. Cada um é dependência do source set correspondente e o Ktor o seleciona automaticamente.
- **Resposta do ViaCEP**: campos extras são ignorados (`ignoreUnknownKeys`), campos ausentes viram texto vazio e o indicador de CEP inexistente é aceito tanto como `"erro": true` quanto como `"erro": "true"`.

## Interface e acessibilidade

O leiaute segue as figuras do enunciado: campo "Digite o CEP", botão "Buscar" com a largura do campo e os cinco resultados logo abaixo. As escolhas visuais seguem as recomendações do Material Design (Google) e do Human Interface Guidelines (Apple):

- **Cores**: esquema Material 3 próprio, em azul, com versões clara e escura escolhidas automaticamente conforme o sistema. Todos os textos têm contraste mínimo de 4,5:1 com o fundo e a borda do campo tem pelo menos 3:1 (nível AA da WCAG 2.1). Exemplos: texto principal 16,3:1 (claro) e 14,4:1 (escuro); texto do botão 5,8:1 e 9,9:1.
- **Estado desabilitado**: mantém a opacidade reduzida padrão do Material, que sinaliza que o controle não está disponível.
- **Tipografia**: fonte [Inter](https://rsms.me/inter/), desenhada para leitura em tela, embutida como recurso Compose para ter a mesma aparência no Android, no iOS e no Desktop. Os tamanhos seguem a escala do Material 3 em `sp`, respeitando o tamanho de texto configurado pelo usuário. A fonte é distribuída sob a SIL Open Font License 1.1; o texto da licença está em [licencas/Inter-OFL.txt](licencas/Inter-OFL.txt).
- **Resultados**: o rótulo aparece em cor secundária e o valor em destaque, para que o dado consultado seja lido primeiro.

## Como executar

### Android

No Android Studio, selecione a configuração **androidApp** e execute em um emulador ou aparelho. Pela linha de comando:

```bash
./gradlew :androidApp:assembleDebug        # Windows: gradlew.bat :androidApp:assembleDebug
./gradlew :androidApp:installDebug         # instala no aparelho/emulador conectado
```

A permissão `android.permission.INTERNET` está declarada em `androidApp/src/main/AndroidManifest.xml`. Todo o tráfego é HTTPS.

### Desktop

```bash
./gradlew :desktopApp:run                  # Windows: gradlew.bat :desktopApp:run
```

### iOS (requer macOS com Xcode)

1. Abra `iosApp/iosApp.xcodeproj` no Xcode.
2. Em *Signing & Capabilities*, escolha seu time de desenvolvimento caso vá rodar em um iPhone físico. No simulador não é necessário.
3. Selecione um simulador (ou aparelho) e clique em **Run**. A fase de build do Xcode compila o framework `Shared` via Gradle.

Os targets configurados são `iosArm64` (aparelho) e `iosSimulatorArm64` (simulador em Macs com Apple Silicon). A interface exibida é a mesma `App()` Compose, por meio de `MainViewController()`; não há tela duplicada em SwiftUI. Como o ViaCEP usa HTTPS, nenhuma exceção de ATS é necessária no `Info.plist`.

## Testes

Os testes ficam em `shared/src/commonTest` e não acessam a internet: a ViewModel usa um repositório falso controlado pelo teste e o repositório usa o `MockEngine` do Ktor.

```bash
./gradlew :shared:jvmTest                  # Desktop/JVM
./gradlew :shared:testAndroidHostTest      # Android (JVM local)
./gradlew :shared:iosSimulatorArm64Test    # iOS (somente no macOS)
```

| Arquivo | O que cobre |
| --- | --- |
| `CepValidationTest` | CEP vazio/incompleto, oito dígitos, caracteres não numéricos, limite de oito dígitos |
| `CepViewModelTest` | Estado durante a consulta, sucesso, limpeza do resultado anterior, CEP inexistente, falha de rede, cliques repetidos |
| `ViaCepRepositoryTest` | Conversão do JSON, campos extras e ausentes, `erro` booleano e texto, status HTTP de erro, corpo inválido, falha de rede |

## Preparando a entrega

Antes de compactar o projeto para o Moodle:

```bash
./gradlew clean                            # Windows: gradlew.bat clean
```

Depois, **não inclua** no arquivo: `build/` (de todos os módulos), `.gradle/`, `.kotlin/`, `.idea/`, `local.properties`, `iosApp/build/`, `xcuserdata/` nem `*.iml`.

## Limitações

- O build e a execução no iOS não foram validados neste ambiente (Windows). O código iOS compila em nível de metadados, mas a geração do framework, o link e a execução no simulador precisam ser verificados em um Mac com Xcode.
- A janela do Android usa o tema claro do sistema até o Compose desenhar a primeira tela; com o aparelho em modo escuro, pode haver um breve clarão na abertura.
- O target `iosX64` (simulador em Macs Intel) não está habilitado, como no projeto gerado pelo Wizard. Se necessário, adicione `iosX64()` à lista de targets em `shared/build.gradle.kts`.
