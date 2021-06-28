package br.com.zup.lista

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.grpc.KeymanagerListaGrpcServiceGrpc
import br.com.zup.grpc.ListaChavePixRequest
import br.com.zup.grpc.TipoDeChave
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.assertThrows
import org.hamcrest.MatcherAssert.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavePixEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub,
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChaveRegex.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChaveRegex.RANDOM, chave = "randomkey-2", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChaveRegex.RANDOM, chave = "randomkey-3", clienteId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // cenário
        val clienteId = CLIENTE_ID.toString()

        // ação
        val response = grpcClient.lista(
            ListaChavePixRequest.newBuilder()
                .setClientId(clienteId)
                .build())

        // validação
        with (response.chavesList) {
            assertThat(this, org.hamcrest.Matchers.hasSize(2))
            assertThat(
                this.map { Pair(it.tipo, it.chave) }.toList(),
                org.hamcrest.Matchers.containsInAnyOrder(
                    Pair(TipoDeChave.RANDOM, "randomkey-3"),
                    Pair(TipoDeChave.EMAIL, "rafael.ponte@zup.com.br")
                )
            )
        }
    }

    /**
     * XXX: será que precisamos disso dado que não existe branch na query?
     */
    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.lista(ListaChavePixRequest.newBuilder()
            .setClientId(clienteSemChaves)
            .build())

        // validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavePixRequest.newBuilder()
                .setClientId(clienteIdInvalido)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub? {
            return KeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChaveRegex,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipodeChave = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}