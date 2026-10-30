package io.ztechnologies.buscacep.domain

/**
 * Endereço correspondente a um CEP, com apenas os campos exibidos na tela.
 *
 * O modelo não conhece HTTP nem o formato JSON do ViaCEP; a conversão
 * fica a cargo da camada de dados.
 */
data class Cep(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String,
)
