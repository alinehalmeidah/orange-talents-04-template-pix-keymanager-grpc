package br.com.zup.chave



import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChave
import br.com.zup.utils.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePix(
    @ValidUUID @field:NotBlank val clienteId: String,
    @field:NotNull val tipoDeChave: TipoDeChave?,
    @field:NotNull val tipoDeConta: TipoDeConta?,
    @field:Size(max = 77) val chave: String
) {

    fun toModel(conta: ContaAssociada): ChavePix{
        return ChavePix(
            clientId = UUID.fromString(this.clienteId),
            tipodeChave = TipoDeChave.valueOf(this.tipoDeChave!!.name),
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            chave = if(this.tipoDeChave == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            conta = conta
        )
    }

}