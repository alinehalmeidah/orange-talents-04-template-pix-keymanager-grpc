package br.com.zup.client

import br.com.zup.grpc.TipoDeConta

enum class AccountType {
    CACC, SVGS;
    companion object {
        fun by(domainType: TipoDeConta): AccountType {
            return when (domainType) {
                TipoDeConta.CONTA_CORRENTE -> CACC
                TipoDeConta.CONTA_POUPANCA -> SVGS
                TipoDeConta.UNKNOWN_TIPO_CONTA -> TODO()
                TipoDeConta.UNRECOGNIZED -> TODO()
            }
        }
    }
}