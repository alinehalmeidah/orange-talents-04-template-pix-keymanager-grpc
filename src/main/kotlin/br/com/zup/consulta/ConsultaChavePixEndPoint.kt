package br.com.zup.consulta

import br.com.zup.chave.ChavePixRepository
import br.com.zup.client.BancoCentralClient
import br.com.zup.config.ErrorHandler
import br.com.zup.grpc.ConsultaChavePixRequest
import br.com.zup.grpc.ConsultaChavePixResponse
import br.com.zup.grpc.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.utils.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator


@ErrorHandler
@Singleton
class ConsultaChavePixEndPoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator
) : KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository, bcbClient)
        responseObserver.onNext(ConsultaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }


}
