package br.com.zup.utils

import br.com.zup.chave.NovaChavePix
import br.com.zup.grpc.RegistraChavePixRequest
import br.com.zup.grpc.TipoDeConta.*
import br.com.zup.grpc.TipoDeChave.*
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex

fun RegistraChavePixRequest.toModel(): NovaChavePix {

    return NovaChavePix(
        clienteId = clientId,
        tipoDeChaveRegex = when(tipoDeChave){
           UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChaveRegex.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when(tipoDeConta){
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}