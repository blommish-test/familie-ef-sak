package no.nav.familie.ef.sak.api.dto

import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import java.time.LocalDate

data class DokumentInfoDto(
        val dokumentInfoId: String,
        val filnavn: String?,
        val tittel: String,
        val journalpostId: String,
        val dato: LocalDate?,
        val journalpostType: Journalposttype)