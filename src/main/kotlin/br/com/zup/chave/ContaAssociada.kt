package br.com.zup.chave

import javax.persistence.Embeddable

@Embeddable
class ContaAssociada(
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String,
    val ispb: String
) {
    override fun toString(): String {
        return "Instituicao: $instituicao, \nTitular: $nomeDoTitular, \nCPF: $cpfDoTitular,  " +
                "\nAgencia: $agencia, \nNumero: $numeroDaConta, \nISPB: $ispb"
    }
}

