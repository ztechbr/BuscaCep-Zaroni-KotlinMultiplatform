package io.ztechnologies.buscacep.domain

/**
 * Regras de entrada do CEP.
 *
 * Internamente o CEP é sempre guardado como oito dígitos, sem hífen; a
 * máscara `00000-000` é apenas visual e fica por conta da interface.
 */
object ValidadorCep {

    const val QUANTIDADE_DIGITOS = 8

    /**
     * Descarta tudo o que não for dígito de 0 a 9 e limita o resultado a
     * oito caracteres. Assim, colar `01001-000` ou digitar letras nunca
     * produz um estado que habilite a busca indevidamente.
     */
    fun normalizar(texto: String): String =
        texto.filter(::ehDigitoAscii).take(QUANTIDADE_DIGITOS)

    fun ehValido(cep: String): Boolean =
        cep.length == QUANTIDADE_DIGITOS && cep.all(::ehDigitoAscii)

    // Char.isDigit() aceitaria dígitos de outros alfabetos, que o ViaCEP não entende.
    private fun ehDigitoAscii(caractere: Char): Boolean = caractere in '0'..'9'
}
