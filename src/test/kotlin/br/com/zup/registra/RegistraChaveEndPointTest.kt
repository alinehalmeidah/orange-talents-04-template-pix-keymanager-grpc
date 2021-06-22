package br.com.zup.registra

import org.junit.jupiter.api.Assertions.*
import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.client.DadosDaContaResponse
import br.com.zup.client.InstituicaoResponse
import br.com.zup.client.ItauClient
import br.com.zup.client.TitularResponse
import br.com.zup.grpc.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.grpc.RegistraChavePixRequest
import br.com.zup.grpc.TipoDeChave
import br.com.zup.grpc.TipoDeConta
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
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.mockito.Mockito.mock



@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val grpcClient: KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {
    @field:Inject
    lateinit var itauClient: ItauClient

    companion object{
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve cadastrar uma nova chave pix(cpf)`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("65640120045")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael M C Ponte", "02467781054")
        )
        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response) {
            assertTrue(chavePixRepository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve cadastrar uma nova chave pix(celular) valida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.PHONE)
            .setChave("+5511911223344")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael M C Ponte", "02467781054")
        )
        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response) {
            assertTrue(chavePixRepository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve cadastrar uma nova chave pix(email) valida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("jonathan@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(CLIENT_ID.toString(),"Rafael M C Ponte", "02467781054")
        )
        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response){
            assertTrue(chavePixRepository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve cadastrar uma nova chave pix(aleatoria) valida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael M C Ponte", "02467781054")
        )
        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response) {
            assertTrue(chavePixRepository.existsByChave(response.pixId.toString()))
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve cadastrar uma nova chave pix(cpf) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("11144466687")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows <StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar uma nova chave pix(celular) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.PHONE)
            .setChave("1111")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows <StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar uma nova chave pix(email) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("jonathanemail.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows <StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando a mesma já existe`() {
        chavePixRepository.save(
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

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("jonathan@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave ja cadastrada*******-", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar uma nova chave pix quando o clientId nao existir no sistema`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("jonathan@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = request.tipoDeConta.name))
            .thenReturn(HttpResponse.notFound())


        val error = assertThrows <StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return mock (ItauClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub? {
            return KeymanagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}
