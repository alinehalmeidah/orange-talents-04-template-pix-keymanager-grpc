package br.com.zup.chave

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long>{
    fun existsByChave(chave: String?): Boolean

    fun findByPixId(pixId: UUID): Optional<ChavePix>

    fun findByPixIdAndClientId(pixId: UUID, clientId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findByClientId(clientId: UUID): List<ChavePix>
}