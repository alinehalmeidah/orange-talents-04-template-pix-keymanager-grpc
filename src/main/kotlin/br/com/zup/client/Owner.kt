package br.com.zup.client

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)