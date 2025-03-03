package no.nav.familie.ef.sak.no.nav.familie.ef.sak.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import no.nav.familie.ef.sak.config.IntegrasjonerConfig
import no.nav.familie.ef.sak.integration.dto.familie.Arbeidsfordelingsenhet
import no.nav.familie.ef.sak.integration.dto.familie.EgenAnsattResponse
import no.nav.familie.ef.sak.integration.dto.familie.Tilgang
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.ef.søknad.Testsøknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime


@Component
class FamilieIntegrasjonerMock(integrasjonerConfig: IntegrasjonerConfig) {

    val responses =
            listOf(
                    WireMock.get(WireMock.urlEqualTo(integrasjonerConfig.pingUri.path))
                            .willReturn(WireMock.aResponse().withStatus(200)),
                    WireMock.post(WireMock.urlEqualTo(integrasjonerConfig.egenAnsattUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(egenAnsatt))),
                    WireMock.post(WireMock.urlEqualTo(integrasjonerConfig.tilgangRelasjonerUri.path))
                            .withRequestBody(WireMock.matching(".*ikkeTilgang.*"))
                            .atPriority(1)
                            .willReturn(WireMock.okJson(objectMapper
                                                                .writeValueAsString(Tilgang(false,
                                                                                            "Mock sier: Du har " +
                                                                                            "ikke tilgang " +
                                                                                            "til person ikkeTilgang")))),
                    WireMock.post(WireMock.urlEqualTo(integrasjonerConfig.tilgangRelasjonerUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(Tilgang(true, null)))),
                    WireMock.get(WireMock.urlEqualTo(integrasjonerConfig.kodeverkPoststedUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(kodeverkPoststed))),
                    WireMock.get(WireMock.urlEqualTo(integrasjonerConfig.kodeverkLandkoderUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(kodeverkLand))),
                    WireMock.post(WireMock.urlEqualTo(integrasjonerConfig.arbeidsfordelingUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(arbeidsfordeling))),

                    WireMock.get(WireMock.urlPathEqualTo(integrasjonerConfig.journalPostUri.path))
                            .withQueryParam("journalpostId", equalTo("1234"))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(journalpost))),
                    WireMock.get(WireMock.urlPathMatching("${integrasjonerConfig.journalPostUri.path}/hentdokument/([0-9]*)/([0-9]*)"))
                            .withQueryParam("variantFormat", equalTo("ORIGINAL"))
                            .willReturn(WireMock.okJson(
                                    objectMapper.writeValueAsString(Ressurs.success(objectMapper.writeValueAsBytes(Testsøknad.søknadOvergangsstønad)))
                            )),
                    WireMock.get(WireMock.urlPathMatching("${integrasjonerConfig.journalPostUri.path}/hentdokument/([0-9]*)/([0-9]*)"))
                            .withQueryParam("variantFormat", equalTo("ARKIV"))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(Ressurs.success(pdfAsBase64String)))),
                    WireMock.put(WireMock.urlMatching("${integrasjonerConfig.dokarkivUri.path}.*"))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(oppdatertJournalpostResponse))),
                    WireMock.post(WireMock.urlMatching("${integrasjonerConfig.dokarkivUri.path}.*"))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(arkiverDokumentResponse))),
                    WireMock.post(WireMock.urlPathEqualTo(integrasjonerConfig.medlemskapUri.path))
                            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(medl))),

                    )

    @Bean("mock-integrasjoner")
    @Profile("mock-integrasjoner")
    fun integrationMockServer(): WireMockServer {
        val mockServer = WireMockServer(8085)
        responses.forEach {
            mockServer.stubFor(it)
        }
        mockServer.start()
        return mockServer
    }

    companion object {

        private val egenAnsatt = Ressurs.success(EgenAnsattResponse(false))
        private val poststed =
                KodeverkDto(mapOf("0575" to listOf(BetydningDto(LocalDate.MIN,
                                                                LocalDate.MAX,
                                                                mapOf("nb" to BeskrivelseDto("OSLO",
                                                                                             "OSLO"))))))
        private val land = KodeverkDto(mapOf("NOR" to listOf(BetydningDto(LocalDate.MIN,
                                                                          LocalDate.MAX,
                                                                          mapOf("nb" to BeskrivelseDto("NORGE",
                                                                                                       "NORGE"))))))
        private val kodeverkPoststed = Ressurs.success(poststed)
        private val kodeverkLand = Ressurs.success(land)

        private val arbeidsfordeling =
                Ressurs.success(listOf(Arbeidsfordelingsenhet("1234", "nerd-enhet")))

        val fnr = "23097825289"
        private val medl =
                Ressurs.success(Medlemskapsinfo(personIdent = fnr,
                                                gyldigePerioder = emptyList(),
                                                uavklartePerioder = emptyList(),
                                                avvistePerioder = emptyList()))

        private val oppdatertJournalpostResponse =
                Ressurs.success(OppdaterJournalpostResponse(journalpostId = "1234"))
        val pdfAsBase64String =
                "JVBERi0xLjIgCjkgMCBvYmoKPDwKPj4Kc3RyZWFtCkJULyA5IFRmKFRlc3QpJyBFVAplbmRzdHJlYW0KZW5kb2JqCjQgMCBvYmoKPDwKL1R5cGUgL1BhZ2UKL1BhcmVudCA1IDAgUgovQ29udGVudHMgOSAwIFIKPj4KZW5kb2JqCjUgMCBvYmoKPDwKL0tpZHMgWzQgMCBSIF0KL0NvdW50IDEKL1R5cGUgL1BhZ2VzCi9NZWRpYUJveCBbIDAgMCA5OSA5IF0KPj4KZW5kb2JqCjMgMCBvYmoKPDwKL1BhZ2VzIDUgMCBSCi9UeXBlIC9DYXRhbG9nCj4+CmVuZG9iagp0cmFpbGVyCjw8Ci9Sb290IDMgMCBSCj4+CiUlRU9G"
        private val arkiverDokumentResponse = Ressurs.success(ArkiverDokumentResponse(journalpostId = "1234", ferdigstilt = true))
        private val journalpost =
                Ressurs.success(Journalpost(journalpostId = "1234",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.MOTTATT,
                                            tema = "ENF",
                                            behandlingstema = "ab0071",
                                            tittel = "abrakadabra",
                                            bruker = Bruker(type = BrukerIdType.FNR, id = fnr),
                                            journalforendeEnhet = "4817",
                                            kanal = "SKAN_IM",
                                            relevanteDatoer = listOf(RelevantDato(LocalDateTime.now(), "DATO_REGISTRERT")),
                                            dokumenter =
                                            listOf(DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "Søknad om overgangsstønad - dokument 1",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"),
                                                                       Dokumentvariant(variantformat = "ORIGINAL"))),
                                                   DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "Søknad om barnetilsyn - dokument 1",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"))),
                                                   DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "Samboeravtale",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"))),
                                                   DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "EtFrykteligLangtDokumentNavnSomTroligIkkeBrekkerOgØdeleggerGUI",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"))),
                                                   DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "Søknad om overgangsstønad - dokument 2",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"))),
                                                   DokumentInfo(dokumentInfoId = "12345",
                                                                tittel = "Søknad om overgangsstønad - dokument 3",
                                                                brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                                                dokumentvarianter =
                                                                listOf(Dokumentvariant(variantformat = "ARKIV"))))))
    }
}