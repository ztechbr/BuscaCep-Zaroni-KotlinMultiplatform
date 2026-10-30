package io.ztechnologies.buscacep

import io.ztechnologies.buscacep.domain.ValidadorCep
import io.ztechnologies.buscacep.presentation.CepUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CepValidationTest {

    @Test
    fun cepVazioMantemBotaoDesabilitado() {
        assertFalse(CepUiState(cep = "").botaoHabilitado)
    }

    @Test
    fun cepIncompletoMantemBotaoDesabilitado() {
        assertFalse(CepUiState(cep = "0100100").botaoHabilitado)
    }

    @Test
    fun oitoDigitosHabilitamBotao() {
        assertTrue(CepUiState(cep = "01001000").botaoHabilitado)
    }

    @Test
    fun cepValidoComConsultaEmAndamentoMantemBotaoDesabilitado() {
        assertFalse(CepUiState(cep = "01001000", carregando = true).botaoHabilitado)
    }

    @Test
    fun normalizacaoDescartaCaracteresNaoNumericos() {
        assertEquals("01001000", ValidadorCep.normalizar("01001-000"))
        assertEquals("123", ValidadorCep.normalizar("a1b2c3"))
        assertEquals("", ValidadorCep.normalizar("abc"))
    }

    @Test
    fun normalizacaoLimitaAOitoDigitos() {
        assertEquals("12345678", ValidadorCep.normalizar("1234567890"))
    }

    @Test
    fun validacaoRejeitaDigitosDeOutrosAlfabetos() {
        // Dígitos arábico-índicos: Char.isDigit() os aceitaria.
        assertFalse(ValidadorCep.ehValido("٠١٢٣٤٥٦٧"))
        assertEquals("", ValidadorCep.normalizar("٠١٢٣٤٥٦٧"))
    }
}
