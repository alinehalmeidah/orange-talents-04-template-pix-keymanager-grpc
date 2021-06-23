package br.com.zup.client

import java.time.LocalDateTime

class RemoveChavePixResponse (
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)