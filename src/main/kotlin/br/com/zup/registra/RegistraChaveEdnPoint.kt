package br.com.zup.registra


import br.com.zup.config.ErrorHandler
import br.com.zup.grpc.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.grpc.RegistraChavePixRequest
import br.com.zup.grpc.RegistraChavePixResponse
import br.com.zup.utils.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndPoint(@Inject private val service: NovaChavePixService)
    : KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceImplBase() {
    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        println(request)
        val novaChave = request!!.toModel()
        val service = service.registra(novaChave)
        responseObserver?.onNext(
            RegistraChavePixResponse.newBuilder()
            .setClientId(service.clientId.toString())
            .setPixId(service.chave)
            .build())
        responseObserver?.onCompleted()
    }
}