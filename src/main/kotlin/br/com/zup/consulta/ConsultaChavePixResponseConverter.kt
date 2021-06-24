package br.com.zup.consulta

import br.com.zup.grpc.ConsultaChavePixResponse
import br.com.zup.grpc.TipoDeChave
import br.com.zup.grpc.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChavePixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo): br.com.zup.grpc.ConsultaChavePixResponse {
        return br.com.zup.grpc.ConsultaChavePixResponse.newBuilder()
            .setClientId(chaveInfo.clientId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(
                br.com.zup.grpc.ConsultaChavePixResponse.ChavePix // 1
                    .newBuilder()
                    .setTipoDeChave(TipoDeChave.valueOf(chaveInfo.tipoDeChave.name)) // 2
                    .setChave(chaveInfo.chave)
                    .setConta(
                        ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder() // 1
                            .setTipoDeConta(TipoDeConta.valueOf(chaveInfo.tipoDeConta.name)) // 2
                            .setInstituicao(chaveInfo.conta.instituicao) // 1 (Conta)
                            .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                            .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}