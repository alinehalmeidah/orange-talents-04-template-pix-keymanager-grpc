package br.com.zup.registra

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ChavePixRepository
import br.com.zup.chave.ContaAssociada
import br.com.zup.client.*
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.mockito.Mockito.mock
import java.time.LocalDateTime


@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val grpcClient: KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {
    @field:Inject
    lateinit var itauClient: ItauClient

    @field:Inject
    lateinit var bcbClient: BancoCentralClient

    companion object{
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael M C Ponte", "02467781054")
        )
    }

    private fun cadastraChavePixRequest(): CadastraChavePixRequest {
        return CadastraChavePixRequest(
            keyType = TipoDeChaveRegex.TELEFONE,
            key = "+5511911223344",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun cadastraChavePixResponse(): CadastraChavePixResponse {
        return CadastraChavePixResponse(
            keyType = TipoDeChaveRegex.TELEFONE,
            key = "+5511911223344",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

    @Test
    fun `deve cadastrar chave pix no banco`(){
        //cenario
        `when` (itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        `when`(bcbClient.create(cadastraChavePixRequest()))
            .thenReturn(HttpResponse.created(cadastraChavePixResponse()))
        //açao
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.PHONE)
                .setChave("+5511911223344")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        //validação
        with(response){
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando nao encontrar dados da conta cliente`(){
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.PHONE)
                    .setChave("+5511911223344")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente nao encontrato no itau", status.description)
        }
    }

    //TODO Corrigir teste
    @Test
    fun `nao deve cadastrar chave pix quando nao for possivel registrar chave no BCB`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(cadastraChavePixRequest()))
            .thenReturn(HttpResponse.badRequest())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.PHONE)
                .setChave("+5511911223344")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando parametros forem invalidos`(){
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)

        }
    }


    @Test
    fun `nao deve cadastrar chave pix quando a mesma ja existe`() {
        chavePixRepository.save(
            ChavePix(
                clientId = CLIENT_ID,
                tipodeChave = TipoDeChaveRegex.EMAIL,
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
            assertEquals("Chave ja cadastrada", status.description)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return mock (ItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClientMock(): BancoCentralClient {
        return mock (BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub? {
            return KeymanagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}