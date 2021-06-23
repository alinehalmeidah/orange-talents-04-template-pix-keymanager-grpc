package br.com.zup.remove

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.client.BancoCentralClient
import br.com.zup.client.RemoveChavePixRequest
import br.com.zup.client.RemoveChavePixResponse
import br.com.zup.grpc.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import org.junit.jupiter.api.Assertions.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class RemoveChavePixEndPointTest(
    val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {

    @field:Inject
    lateinit var bcbClient: BancoCentralClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        CHAVE_EXISTENTE = chavePixRepository.save(chave(
            tipoDaChave = TipoDeChaveRegex.EMAIL,
            chave = "jonathan@gmail.com",
            clienteId = UUID.randomUUID()
        ))
    }

    private fun chave(
        tipoDaChave: TipoDeChaveRegex,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipodeChave = tipoDaChave,
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

    @Test
    fun `deve excluir uma chave pix`() {

        Mockito.`when`(bcbClient.delete("adri@gmail.com", RemoveChavePixRequest("adri@gmail.com")))
            .thenReturn(HttpResponse.ok(
                RemoveChavePixResponse(key = "adri@gmail.com",
                    participant = "60701190",
                    deletedAt = LocalDateTime.now())
            )
            )

        val response = grpcClient.remove(br.com.zup.grpc.RemoveChavePixRequest.newBuilder()
            .setClientId(CHAVE_EXISTENTE.clientId.toString())
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .build())

        with(response){
            assertEquals(CHAVE_EXISTENTE.clientId.toString(), clientId)
            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave inexistente`() {
        val chavePixDeferente = UUID.randomUUID()
        // ação
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                br.com.zup.grpc.RemoveChavePixRequest.newBuilder()
                    .setPixId(chavePixDeferente.toString())
                    .setClientId(CHAVE_EXISTENTE.clientId.toString())
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao cadastrada ou nao pertence ao cliente", status.description)
        }
    }


    @MockBean(BancoCentralClient::class)
    fun bcbClientMock(): BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub? {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}
