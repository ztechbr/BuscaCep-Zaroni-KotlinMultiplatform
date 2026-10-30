package io.ztechnologies.buscacep.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import buscacep.shared.generated.resources.Res
import buscacep.shared.generated.resources.inter_medium
import buscacep.shared.generated.resources.inter_regular
import buscacep.shared.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/**
 * Tema do aplicativo, com versões clara e escura que acompanham a
 * preferência do sistema (Android, iOS e Desktop).
 *
 * As cores seguem os papéis do Material 3. Todos os pares de texto sobre
 * fundo usados na tela atingem pelo menos 4,5:1 de contraste (WCAG AA) e
 * a borda do campo atinge pelo menos 3:1, como recomendam as diretrizes
 * de acessibilidade do Material Design e do Human Interface Guidelines.
 *
 * A tipografia usa a fonte Inter, embutida no aplicativo como recurso
 * Compose. Assim o texto tem a mesma aparência nas três plataformas, sem
 * depender das fontes instaladas em cada sistema. Os tamanhos continuam em
 * `sp` (escala tipográfica padrão do Material 3), respeitando o ajuste de
 * tamanho de texto feito pelo usuário nas configurações de acessibilidade.
 */
@Composable
fun TemaBuscaCep(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CoresEscuras else CoresClaras,
        typography = tipografiaInter(),
        content = conteudo,
    )
}

/**
 * Inter foi desenhada para leitura em tela: altura-x generosa e formas
 * bem distintas entre caracteres parecidos (como 1, l e I), o que ajuda na
 * conferência de números de CEP. Licença SIL OFL 1.1, em `licencas/`.
 */
@Composable
private fun tipografiaInter(): Typography {
    val inter = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
    )
    return remember(inter) {
        val padrao = Typography()
        fun TextStyle.comInter() = copy(fontFamily = inter)
        Typography(
            displayLarge = padrao.displayLarge.comInter(),
            displayMedium = padrao.displayMedium.comInter(),
            displaySmall = padrao.displaySmall.comInter(),
            headlineLarge = padrao.headlineLarge.comInter(),
            headlineMedium = padrao.headlineMedium.comInter(),
            headlineSmall = padrao.headlineSmall.comInter(),
            titleLarge = padrao.titleLarge.comInter(),
            titleMedium = padrao.titleMedium.comInter(),
            titleSmall = padrao.titleSmall.comInter(),
            bodyLarge = padrao.bodyLarge.comInter(),
            bodyMedium = padrao.bodyMedium.comInter(),
            bodySmall = padrao.bodySmall.comInter(),
            labelLarge = padrao.labelLarge.comInter(),
            labelMedium = padrao.labelMedium.comInter(),
            labelSmall = padrao.labelSmall.comInter(),
        )
    }
}

private val CoresClaras: ColorScheme = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    surfaceContainerLow = Color(0xFFF3F3FA),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    inverseSurface = Color(0xFF2E3036),
    inverseOnSurface = Color(0xFFF0F0F7),
    inversePrimary = Color(0xFFA8C7FA),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

private val CoresEscuras: ColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerLow = Color(0xFF191C20),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF2E3036),
    inversePrimary = Color(0xFF0B57D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)
