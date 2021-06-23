package br.com.zup.remove

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.grpc.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.grpc.RemoveChavePixRequest
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChavePixEndPointTest(
    val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {

    companion object {
        val CLIENT_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve excluir uma chave pix`() {

        val chave = chavePixRepository.save(
            ChavePix(
                clientId = CLIENT_ID,
                tipodeChaveRegex = TipoDeChaveRegex.EMAIL,
                chave = "jonathan@email.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Rafael M C Ponte",
                    cpfDoTitular = "02467781054",
                    agencia = "0001",
                    numeroDaConta = "291900"
                )
            )
        )

        val request = RemoveChavePixRequest.newBuilder()
            .setPixId(chave.pixId.toString())
            .setClientId(CLIENT_ID.toString())
            .build()
        val response = grpcClient.remove(request)

        with(response) {
            assertEquals(clientId.toString(),request.clientId.toString())
            assertNotNull(chavePixRepository.findById(chave.id))
        }
    }

    @Test
    fun `nao deve excluir uma chave pix quando nao pertence ao cliente`() {
        val CLIENT_ID_1 = UUID.randomUUID()
        val CLIENT_ID_2 = UUID.randomUUID()

        val chave1 = chavePixRepository.save(
            ChavePix(
                clientId = CLIENT_ID_1,
                tipodeChaveRegex  = TipoDeChaveRegex.EMAIL,
                chave = "jonathan@email.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Rafael M C Ponte",
                    cpfDoTitular = "02467781054",
                    agencia = "0001",
                    numeroDaConta = "291900"
                )
            )
        )
        val chave2 = chavePixRepository.save(
            ChavePix(
                clientId = CLIENT_ID_2,
                tipodeChaveRegex= TipoDeChaveRegex.EMAIL,
                chave = "jones@email.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Yuri Matheus",
                    cpfDoTitular = "86135457004",
                    agencia = "0001",
                    numeroDaConta = "123455"
                )
            )
        )

        val request = RemoveChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID_2.toString())
            .setPixId(chave1.pixId.toString())
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve excluir uma chave pix quando a mesma nao existe`(){
        val request = RemoveChavePixRequest.newBuilder()
            .setPixId(PIX_ID.toString())
            .setClientId(CLIENT_ID.toString())
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub? {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}