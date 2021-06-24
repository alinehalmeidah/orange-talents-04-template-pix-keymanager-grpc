package br.com.zup.utils

import br.com.zup.chave.NovaChavePix
import br.com.zup.consulta.Filtro
import br.com.zup.grpc.ConsultaChavePixRequest
import br.com.zup.grpc.ConsultaChavePixRequest.FiltroCase.*
import br.com.zup.grpc.RegistraChavePixRequest
import br.com.zup.grpc.TipoDeConta.*
import br.com.zup.grpc.TipoDeChave.*
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import javax.validation.ConstraintViolationException
import javax.validation.Validator

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

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase!!) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clientId = it.clientId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}
