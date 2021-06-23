package br.com.zup.chave

import br.com.zup.grpc.TipoDeConta
import br.com.zup.registra.TipoDeChaveRegex
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(@field:NotNull
               @Column(nullable = false, columnDefinition = "BINARY(16)")
               val clientId: UUID,

               @field:NotNull
               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipodeChave: TipoDeChaveRegex,

               @field:NotBlank
               @Column(unique = true, nullable = false)
               var chave: String,

               @field:Valid
               @Embedded
               val conta: ContaAssociada,

               @field:NotNull
               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipoDeConta: TipoDeConta
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    val pixId: UUID = UUID.randomUUID()
    val criadaEm: LocalDateTime = LocalDateTime.now()
}