package br.com.zup.chave

import javax.persistence.Embeddable

@Embeddable
class ContaAssociada(
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String
) {
    override fun toString(): String {
        return "Instituicao: $instituicao, \nTitular: $nomeDoTitular, \nCPF: $cpfDoTitular,  " +
                "\nAgencia: $agencia, \nNumero: $numeroDaConta"
    }

    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}

