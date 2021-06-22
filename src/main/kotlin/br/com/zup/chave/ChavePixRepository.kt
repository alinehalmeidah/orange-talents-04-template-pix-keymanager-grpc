package br.com.zup.chave

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long>{
    fun existsByChave(chave: String?): Boolean

    fun findByPixIdAndClientId(pixId: UUID, clientId: UUID): Optional<ChavePix>

    @Query("select * from chave_pix where pix_id=:pixId and client_id=:clientId", nativeQuery = true)
    fun consultarChaveComDono(pixId: UUID, clientId: UUID): Optional<ChavePix>
}