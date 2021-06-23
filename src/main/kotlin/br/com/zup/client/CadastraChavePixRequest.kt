package br.com.zup.client

import br.com.zup.registra.TipoDeChaveRegex

data class CadastraChavePixRequest(
    val keyType: TipoDeChaveRegex,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)