package br.com.zup.client

import br.com.zup.registra.TipoDeChaveRegex
import java.time.LocalDateTime

data class CadastraChavePixResponse(
    val keyType: TipoDeChaveRegex,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)