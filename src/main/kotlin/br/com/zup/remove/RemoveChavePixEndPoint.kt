package br.com.zup.remove

import br.com.zup.config.ErrorHandler
import br.com.zup.grpc.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.grpc.RemoveChavePixRequest
import br.com.zup.grpc.RemoveChavePixResponse
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixEndPoint(@Inject private val service: RemoveChavePixService)
    : KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase(){

    override fun remove(request: RemoveChavePixRequest?, responseObserver: StreamObserver<RemoveChavePixResponse>?) {
        service.remove(request?.clientId, request?.pixId)

        responseObserver?.onNext(
            RemoveChavePixResponse.newBuilder()
                .setClientId(request?.clientId)
                .setPixId(request?.pixId)
                .build())
        responseObserver?.onCompleted()
    }
}