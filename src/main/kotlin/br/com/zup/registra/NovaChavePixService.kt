package br.com.zup.registra


import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.NovaChavePix
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
                          @Inject val itauClient: ItauClient
) {

    private val Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix?): ChavePix {

        //Verifica se a chave ja existe no banco
        if(repository.existsByChave(novaChavePix?.chave)) {
            throw ChavePixExistenteException("Chave ja cadastrada")
        }

        //Consulta no sistema do ERP do ITAU Client
        val itauClientResponse = itauClient.buscaContaPorTipo(novaChavePix?.clienteId!!, novaChavePix.tipoDeConta!!.name)
        val conta = itauClientResponse.body()?.toModel() ?: throw IllegalStateException("Cliente nao encontrato no itau")

        //Salva no banco de dados
        val novaChave = novaChavePix.toModel(conta)
        repository.save(novaChave)
        return novaChave
    }
}