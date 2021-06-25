package br.com.zup.consulta

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.client.*
import br.com.zup.grpc.ConsultaChavePixRequest
import br.com.zup.grpc.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import br.com.zup.utils.violations
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndPointTest(
    val grpcClient: KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChaveRegex.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChaveRegex.CPF, chave = "63657520325", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChaveRegex.RANDOM, chave = "randomkey-3", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChaveRegex.TELEFONE, chave = "+551155554321", clienteId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {
        // cenário
        val chaveExistente = repository.findByChave("+551155554321").get()

        // ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                    .setPixId(chaveExistente.pixId.toString())
                    .setClientId(chaveExistente.clientId.toString())
                    .build()
                ).build())

        // validação
        with(response) {
            assertEquals(chaveExistente.pixId.toString(), this.pixId)
            assertEquals(chaveExistente.clientId.toString(), this.clientId)
            assertEquals(chaveExistente.tipodeChave.name, this.chave.tipoDeChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        // ação
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows <StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClientId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir localmente`() {
        // cenário
        val chaveExistente = repository.findByChave("rafael.ponte@zup.com.br").get()

        // ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("rafael.ponte@zup.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.pixId.toString(), this.pixId)
            assertEquals(chaveExistente.clientId.toString(), this.clientId)
            assertEquals(chaveExistente.tipodeChave.name, this.chave.tipoDeChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {
        // cenário
        val bcbResponse = pixKeyDetailsResponse()
        `when`(bcbClient.findByKey(key = "user.from.another.bank@santander.com.br"))
            .thenReturn(HttpResponse.ok())

        // ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("user.from.another.bank@santander.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clientId)
            assertEquals(bcbResponse.keyType.name, this.chave.tipoDeChave.name)
            assertEquals(bcbResponse.key, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        // cenário
        `when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("not.existing.user@santander.com.br")
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }
    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                            .setPixId("")
                            .setClientId("")
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(
                violations(), containsInAnyOrder(
                    Pair("pixId", "não deve estar em branco"),
                    Pair("clientId", "não deve estar em branco"),
                    Pair("pixId", "não é um formato válido de UUID"),
                    Pair("clientId", "não é um formato válido de UUID"),
                )
            )
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().setChave("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(
                violations(), containsInAnyOrder(
                    Pair("chave", "não deve estar em branco"),
                )
            )
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub? {
            return KeymanagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChaveRegex,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipodeChave= tipo,
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

    private fun pixKeyDetailsResponse(): ConsultaChavePixResponse {
        return ConsultaChavePixResponse(
            keyType = TipoDeChaveRegex.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }

}