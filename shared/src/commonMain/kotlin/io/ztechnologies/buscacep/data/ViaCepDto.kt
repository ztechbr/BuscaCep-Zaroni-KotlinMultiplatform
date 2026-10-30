package io.ztechnologies.buscacep.data

import io.ztechnologies.buscacep.domain.Cep
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Formato da resposta do ViaCEP. Campos ausentes recebem texto vazio e
 * propriedades extras (ibge, ddd, siafi...) são ignoradas pela configuração
 * do [kotlinx.serialization.json.Json].
 */
@Serializable
internal data class ViaCepDto(
    val cep: String = "",
    val logradouro: String = "",
    val bairro: String = "",
    val localidade: String = "",
    val uf: String = "",
    // O ViaCEP já devolveu tanto `"erro": true` quanto `"erro": "true"`;
    // ler como JsonElement aceita as duas formas.
    val erro: JsonElement? = null,
) {

    val indicaCepInexistente: Boolean
        get() = (erro as? JsonPrimitive)?.booleanOrNull == true

    fun paraDominio(): Cep = Cep(
        cep = cep,
        logradouro = logradouro,
        bairro = bairro,
        localidade = localidade,
        uf = uf,
    )
}
