package br.com.zup.remove

import br.com.zup.chave.ChavePixRepository
import br.com.zup.config.ChavePixNaoEncontradaException
import br.com.zup.utils.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(@Inject val repository: ChavePixRepository,
){

    @Transactional
    fun remove(@NotBlank @ValidUUID(message = "Cliente ID com formato invalido") clientId: String?,
               @NotBlank @ValidUUID(message = "Pix ID com formato invalido") pixId: String?
    ){
        val uuidPixId = UUID.fromString(pixId)
        val uuidClientId = UUID.fromString(clientId)

        println("$uuidPixId ----------------- $uuidClientId")

        val chave = repository.findByPixIdAndClientId(pixId =  uuidPixId, clientId =  uuidClientId)
            .orElseThrow{ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }


        repository.delete(chave)
    }
}