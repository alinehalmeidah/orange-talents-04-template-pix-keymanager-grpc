package br.com.zup.lista

import br.com.zup.chave.ChavePixRepository
import br.com.zup.config.ErrorHandler
import br.com.zup.grpc.*
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavePixEndPoint(@Inject private val repository: ChavePixRepository)
    : KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceImplBase(){
    override fun lista(request: ListaChavePixRequest?, responseObserver: StreamObserver<ListaChavePixResponse>?) {
        val uuidClientId = UUID.fromString(request?.clientId)
        val chaves = repository.findByClientId(uuidClientId).map {
            ListaChavePixResponse.ChavePix.newBuilder()
                .setPixId(it.pixId.toString())
                .setTipo(TipoDeChave.valueOf(it.tipodeChave.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }
        responseObserver?.onNext(
            ListaChavePixResponse.newBuilder()
                .setClientId(uuidClientId.toString())
                .addAllChaves(chaves)
                .build()
        )
        responseObserver?.onCompleted()
    }
}