package io.ztechnologies.buscacep.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Exibe o CEP no formato `00000-000` sem alterar o valor guardado no
 * estado, que continua com apenas os oito dígitos.
 */
internal object MascaraCep : VisualTransformation {

    private const val POSICAO_HIFEN = 5

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val temHifen = digitos.length > POSICAO_HIFEN
        val formatado = if (temHifen) {
            digitos.take(POSICAO_HIFEN) + "-" + digitos.drop(POSICAO_HIFEN)
        } else {
            digitos
        }

        val mapeamento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (temHifen && offset > POSICAO_HIFEN) offset + 1 else offset

            override fun transformedToOriginal(offset: Int): Int =
                if (temHifen && offset > POSICAO_HIFEN) offset - 1 else offset
        }
        return TransformedText(AnnotatedString(formatado), mapeamento)
    }
}
