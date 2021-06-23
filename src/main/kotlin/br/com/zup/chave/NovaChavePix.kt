package br.com.zup.chave

import br.com.zup.client.CadastraChavePixResponse
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import br.com.zup.utils.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePix(
    @ValidUUID @field:NotBlank val clienteId: String,
    @field:NotNull val tipoDeChaveRegex: TipoDeChaveRegex?,
    @field:NotNull val tipoDeConta: TipoDeConta?,
    @field:Size(max = 77) val chave: String
) {

    fun toModel(conta: ContaAssociada,bcbResponse: CadastraChavePixResponse): ChavePix{
        return ChavePix(
            clientId = UUID.fromString(this.clienteId),
            tipodeChaveRegex = TipoDeChaveRegex.valueOf(this.tipoDeChaveRegex!!.name),
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            chave = bcbResponse.key,
            conta = conta
        )
    }



}