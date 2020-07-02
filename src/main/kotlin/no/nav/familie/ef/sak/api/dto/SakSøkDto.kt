package no.nav.familie.ef.sak.api.dto

import java.util.*

data class SakSøkListeDto(val saker: List<SakSøkDto>)

data class SakSøkDto(val sakId: UUID,
                     val personIdent: String,
                     val navn: NavnDto,
                     val kjønn: Kjønn)
