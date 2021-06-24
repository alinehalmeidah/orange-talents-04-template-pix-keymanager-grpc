package br.com.zup.consulta

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ContaAssociada
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clientId: UUID? = null,
    val tipoDeChave: TipoDeChaveRegex,
    val chave: String,
    val tipoDeConta: TipoDeConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.pixId,
                clientId = chave.clientId,
                tipoDeChave = chave.tipodeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}