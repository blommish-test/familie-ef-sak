package no.nav.familie.ef.sak.config

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Configuration
class PdlConfig(@Value("\${PDL_URL}") pdlUrl: URI) {

    val pdlUri: URI = UriComponentsBuilder.fromUri(pdlUrl).path(PATH_GRAPHQL).build().toUri()

    companion object {

        const val PATH_GRAPHQL = "graphql"

        val personBolkKortQuery = graphqlQuery("/pdl/person_kort_bolk.graphql")

        val søkerKortQuery = graphqlQuery("/pdl/søker_kort.graphql")

        val søkerQuery = graphqlQuery("/pdl/søker.graphql")

        val barnQuery = graphqlQuery("/pdl/barn.graphql")

        val annenForelderQuery = graphqlQuery("/pdl/annenForelder.graphql")

        private fun graphqlQuery(path: String) = PdlConfig::class.java.getResource(path)
                .readText()
                .graphqlCompatible()

        private fun String.graphqlCompatible(): String {
            return StringUtils.normalizeSpace(this.replace("\n", ""))
        }
    }
}
