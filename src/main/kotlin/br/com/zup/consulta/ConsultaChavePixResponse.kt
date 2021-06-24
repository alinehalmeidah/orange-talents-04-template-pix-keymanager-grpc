package br.com.zup.consulta

import br.com.zup.Instituicoes
import br.com.zup.chave.ContaAssociada
import br.com.zup.client.AccountType
import br.com.zup.client.BankAccount
import br.com.zup.client.Owner
import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import java.time.LocalDateTime

class ConsultaChavePixResponse(
    val keyType: TipoDeChaveRegex,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoDeChave = keyType,
            chave = this.key,
            tipoDeConta = when(this.bankAccount.accountType) {
                AccountType.CACC -> TipoDeConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoDeConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber,

                )

        )
    }
}