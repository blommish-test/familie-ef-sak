package no.nav.familie.ef.sak.integration.dto.pdl

data class PdlHentGeografiskTilknytning(val hentGeografiskTilknytning: PdlGeografiskTilknytning?)

data class PdlGeografiskTilknytning(val gtType: String?,
                                    val gtKommune: String?,
                                    val gtBydel: String?,
                                    val gtLand: String?){
    /*fun hentGeografiskTilknytning(): String {
        return when (gtType) {
            GeografiskTilknytningType.KOMMUNE -> gtKommune!!
            GeografiskTilknytningType.BYDEL -> gtBydel!!
            GeografiskTilknytningType.UTLAND -> gtLand!!
            GeografiskTilknytningType.UDEFINERT -> "ingen geografisk tilknytning"
            null -> "fant ingen geografisktilknytning"
        }
    }*/
}

enum class GeografiskTilknytningType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}

data class PdlGeografiskTilknytningRequest(val variables: PdlGeografiskTilknytningVariables,
                                           val query: String)

data class PdlGeografiskTilknytningVariables(val ident: String)