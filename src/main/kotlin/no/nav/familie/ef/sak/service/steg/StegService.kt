package no.nav.familie.ef.sak.service.steg

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.sak.repository.domain.Behandling
import no.nav.familie.ef.sak.service.steg.StegType.BEHANDLING_FERDIGSTILT
import no.nav.familie.ef.sak.sikkerhet.SikkerhetContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StegService(
        private val behandlingSteg: List<BehandlingSteg<*>>,
) {

    private val stegSuksessMetrics: Map<StegType, Counter> = initStegMetrikker("suksess")

    private val stegFeiletMetrics: Map<StegType, Counter> = initStegMetrikker("feil")


    @Transactional
    fun håndterSøknad(behandling: Behandling, søknad: String): Behandling {
        val behandlingSteg: RegistrereSøknadSteg = hentBehandlingSteg(StegType.REGISTRERE_SØKNAD) as RegistrereSøknadSteg
        return håndterSteg(behandling, behandlingSteg) {
            behandlingSteg.utførStegOgAngiNeste(behandling, søknad)
        }
    }


    // Generelle stegmetoder
    private fun håndterSteg(behandling: Behandling,
                            behandlingSteg: BehandlingSteg<*>,
                            utførendeSteg: () -> StegType): Behandling {
        val stegType = behandlingSteg.stegType()
        try {
            val saksbehandlerNavn = SikkerhetContext.hentSaksbehandlerNavn()
            val behandlerRolle =
                    SikkerhetContext.hentBehandlerRolleForSteg(behandling.steg.tillattFor.minByOrNull { it.nivå })

            LOG.info("$saksbehandlerNavn håndterer $stegType på behandling ${behandling.id}")
            if (!behandling.steg.tillattFor.contains(behandlerRolle)) {
                error("$saksbehandlerNavn kan ikke utføre steg '${stegType.displayName()} pga manglende rolle.")
            }

            if (behandling.steg == BEHANDLING_FERDIGSTILT) {
                error("Behandlingen er avsluttet og stegprosessen kan ikke gjenåpnes")
            }

            if (stegType.erSaksbehandlerSteg() && stegType.kommerEtter(behandling.steg)) {
                error("$saksbehandlerNavn prøver å utføre steg '${stegType.displayName()}', men behandlingen er på steg '${behandling.steg.displayName()}'")
            }

            if (behandling.steg == StegType.BESLUTTE_VEDTAK && stegType != StegType.BESLUTTE_VEDTAK) {
                error("Behandlingen er på steg '${behandling.steg.displayName()}', og er da låst for alle andre type endringer.")
            }

            behandlingSteg.preValiderSteg(behandling, this)
            val nesteSteg = utførendeSteg()
            behandlingSteg.postValiderSteg(behandling)

            stegSuksessMetrics[stegType]?.increment()

            if (nesteSteg == BEHANDLING_FERDIGSTILT) {
                LOG.info("$saksbehandlerNavn er ferdig med stegprosess på behandling ${behandling.id}")
            }

            // TODO: Venter på merge fra Johan og Martine sin branch med behandlingService
//            if (!nesteSteg.erGyldigIKombinasjonMedStatus(behandlingService.hentBehandling(behandling.id).status)) {
//                error("Steg '${nesteSteg.displayName()}' kan ikke settes på behandling i kombinasjon med status ${behandling.status}")
//            }
//
//            val returBehandling = behandlingService.oppdaterStegPåBehandling(behandlingId = behandling.id, steg = nesteSteg)
//
//            LOG.info("$saksbehandlerNavn har håndtert $stegType på behandling ${behandling.id}")
//            return returBehandling

            return behandling
        } catch (exception: Exception) {
            stegFeiletMetrics[stegType]?.increment()
            LOG.error("Håndtering av stegtype '$stegType' feilet på behandling ${behandling.id}.")
            secureLogger.info("Håndtering av stegtype '$stegType' feilet.",
                              exception)
            throw exception
        }
    }

    fun hentBehandlingSteg(stegType: StegType): BehandlingSteg<*>? {
        return behandlingSteg.firstOrNull { it.stegType() == stegType }
    }

    private fun initStegMetrikker(type: String): Map<StegType, Counter> {
        return behandlingSteg.map {
            it.stegType() to Metrics.counter("behandling.steg.$type",
                                             "steg",
                                             it.stegType().name,
                                             "beskrivelse",
                                             it.stegType().rekkefølge.toString() + " " + it.stegType().displayName())
        }.toMap()
    }

    companion object {

        val LOG = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
