package br.com.zup.client

import br.com.zup.registra.TipoDeChaveRegex
import java.time.LocalDateTime

class ConsultaChavePixResponse(
    val keyType: TipoDeChaveRegex,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)