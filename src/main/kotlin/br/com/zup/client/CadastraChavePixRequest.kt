package br.com.zup.client

import br.com.zup.chave.ChavePix
import br.com.zup.chave.ContaAssociada
import br.com.zup.registra.TipoDeChaveRegex

data class CadastraChavePixRequest(
    val keyType: TipoDeChaveRegex,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
){

    companion object {

        fun of(chave: ChavePix): CadastraChavePixRequest {
            return CadastraChavePixRequest(
                keyType = chave.tipodeChave,
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroDaConta,
                    accountType = AccountType.by(chave.tipoDeConta)
                ),
                owner = Owner(
                    type = OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeDoTitular,
                    taxIdNumber = chave.conta.cpfDoTitular
                )
            )
        }
    }
}