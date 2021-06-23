package br.com.zup.registra

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.NovaChavePix
import br.com.zup.client.BancoCentralClient
import br.com.zup.client.ItauClient
import br.com.zup.config.ChavePixExistenteException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ItauClient,
                          @Inject val bcbClient: BancoCentralClient
) {

    private val Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix?): ChavePix {

        //Verificificando
        if (repository.existsByChave(novaChavePix?.chave)) {
            throw ChavePixExistenteException("Chave ja cadastrada")
        }

        //Consultando
        val itauClientResponse =
            itauClient.buscaContaPorTipo(novaChavePix?.clienteId!!, novaChavePix.tipoDeConta!!.name)
        val conta =
            itauClientResponse.body()?.toModel() ?: throw IllegalStateException("Cliente nao encontrato no itau")

        Logger.info("Busca pela conta concluído com sucesso")

        //Cadastrando  (BCB)
        val bcbResponse = bcbClient.create(novaChavePix.toBcb(conta))
        Logger.info("Registrando chave Pix no Banco Central do Brasil")

        //Salvando
        val novaChave = novaChavePix.toModel(conta, bcbResponse.body()!!)
        repository.save(novaChave)
        Logger.info("Chave Pix salva com sucesso no sistema")
        return novaChave
    }
}