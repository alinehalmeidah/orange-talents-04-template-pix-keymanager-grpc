package br.com.zup.consulta

import br.com.zup.chave.ChavePixRepository
import br.com.zup.client.BancoCentralClient
import br.com.zup.config.ChavePixNaoEncontradaException
import br.com.zup.utils.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field: ValidUUID val clientId: String,
        @field:NotBlank @field: ValidUUID val pixId: String
    ) : Filtro () {
        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clientIdAsUuid() = UUID.fromString(clientId)
        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findByPixId(pixIdAsUuid())
                .filter { it.pertenceAo(clientIdAsUuid()) }
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String): Filtro() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.findByKey(chave)
                    when(response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixNaoEncontradaException("Chave Pix inexistente")
                    }
                }
        }

    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
