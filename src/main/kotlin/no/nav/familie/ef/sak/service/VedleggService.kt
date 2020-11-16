package no.nav.familie.ef.sak.service

import no.nav.familie.ef.sak.api.dto.DokumentInfoDto
import no.nav.familie.ef.sak.domene.DokumentVariantformat
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import org.springframework.stereotype.Service
import java.util.*

@Service
class VedleggService(private val behandlingService: BehandlingService,
                     private val journalføringService: JournalføringService) {


    fun finnVedleggForBehandling(behandlingId: UUID): List<DokumentInfoDto> {
        val behandling = behandlingService.hentBehandling(behandlingId)
        return behandling.journalposter
                .map { journalføringService.hentJournalpost(it.journalpostId) }
                .flatMap { journalpost ->
                    journalpost.dokumenter?.map { tilDokumentInfoDto(it, journalpost) } ?: emptyList()
                }
    }

    private fun tilDokumentInfoDto(dokumentInfo: DokumentInfo,
                                   journalpost: Journalpost): DokumentInfoDto {
        return DokumentInfoDto(
                dokumentInfoId = dokumentInfo.dokumentInfoId,
                filnavn = dokumentInfo.dokumentvarianter?.find { it.variantformat == DokumentVariantformat.ARKIV.toString() }?.filnavn,
                tittel = dokumentInfo.tittel ?: "Tittel mangler",
                journalpostId = journalpost.journalpostId,
                dato = journalpost.datoMottatt?.toLocalDate(),
                journalpostType = journalpost.journalposttype
        )
    }

}