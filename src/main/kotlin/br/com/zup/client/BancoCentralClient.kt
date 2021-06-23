package br.com.zup.client

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BancoCentralClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun create(@Body request: CadastraChavePixRequest): HttpResponse<CadastraChavePixResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun delete(@PathVariable key: String, @Body request: RemoveChavePixRequest): HttpResponse<RemoveChavePixResponse>

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<ConsultaChavePixResponse>

}