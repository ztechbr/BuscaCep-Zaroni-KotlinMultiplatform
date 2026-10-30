package io.ztechnologies.buscacep.data

import io.ztechnologies.buscacep.domain.Cep
import io.ztechnologies.buscacep.domain.FalhaConsultaCep

/**
 * Consulta os dados de endereço correspondentes a um CEP.
 *
 * A abstração existe para que a ViewModel possa ser testada com um
 * repositório falso, sem depender de rede.
 */
interface CepRepository {

    /**
     * @param cep oito dígitos, sem máscara.
     * @throws FalhaConsultaCep quando o CEP não existe ou o serviço não pôde ser consultado.
     */
    suspend fun buscarCep(cep: String): Cep
}
