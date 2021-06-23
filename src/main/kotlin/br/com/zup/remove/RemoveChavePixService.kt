package br.com.zup.remove

import br.com.zup.chave.ChavePixRepository
import br.com.zup.client.BancoCentralClient
import br.com.zup.client.RemoveChavePixRequest
import br.com.zup.config.ChavePixNaoEncontradaException
import br.com.zup.utils.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank


@Validated
@Singleton
class RemoveChavePixService(@Inject val repository: ChavePixRepository,
                            @Inject val bcbClient: BancoCentralClient

) {
    @Transactional
    fun remove(@NotBlank @ValidUUID(message = "Cliente ID com formato invalido") clientId: String?,
               @NotBlank @ValidUUID(message = "Pix ID com formato invalido") pixId: String?
    ){
        //Convertendo a chave e id do cliente de String para UUID
        val uuidPixId = UUID.fromString(pixId)
        val uuidClientId = UUID.fromString(clientId)

        println("$uuidPixId ----------------- $uuidClientId")

        //Buscando os dados da chave pix pela chave e id do cliente
        val chave = repository.findByPixIdAndClientId(pixId =  uuidPixId, clientId =  uuidClientId)
            .orElseThrow{ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }


        //Criando a requisição para o Banco Central do Brasil (BCB)
        val request = RemoveChavePixRequest(key= chave.chave)

        //Mandando a requisição para o BCB para remover a chave Pix
        val bcbResponse = bcbClient.delete(key= chave.chave, request= request)
        check(bcbResponse.status != HttpStatus.OK){"Erro ao remover a chave Pix do Banco Central do Brasil"}

        repository.delete(chave)
    }
}
