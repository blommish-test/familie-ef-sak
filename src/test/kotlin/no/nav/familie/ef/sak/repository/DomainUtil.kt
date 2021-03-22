package no.nav.familie.ef.sak.no.nav.familie.ef.sak.repository

import no.nav.familie.ef.sak.repository.domain.*
import no.nav.familie.ef.sak.service.steg.StegType
import no.nav.familie.ef.sak.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

fun oppgave(behandling: Behandling, erFerdigstilt: Boolean = false): Oppgave =
        Oppgave(behandlingId = behandling.id,
                gsakOppgaveId = 123,
                type = Oppgavetype.Journalføring,
                erFerdigstilt = erFerdigstilt)

fun behandling(fagsak: Fagsak,
               aktiv: Boolean = true,
               status: BehandlingStatus = BehandlingStatus.OPPRETTET,
               steg: StegType = StegType.VILKÅR,
               oppdragId: UUID = UUID.randomUUID(),
               type: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING): Behandling =
        Behandling(fagsakId = fagsak.id,
                   id = oppdragId,
                   type = type,
                   status = status,
                   steg = steg,
                   aktiv = aktiv,
                   resultat = BehandlingResultat.IKKE_SATT)


fun fagsak(identer: Set<FagsakPerson> = setOf(), stønadstype: Stønadstype = Stønadstype.OVERGANGSSTØNAD) =
        Fagsak(stønadstype = stønadstype, søkerIdenter = identer)

fun vilkårsvurdering(behandlingId: UUID,
                     resultat: Vilkårsresultat,
                     type: VilkårType,
                     delvilkårsvurdering: List<Delvilkårsvurdering> = emptyList()): Vilkårsvurdering =
        Vilkårsvurdering(behandlingId = behandlingId,
                         resultat = resultat,
                         type = type,
                         delvilkårsvurdering = DelvilkårsvurderingWrapper(delvilkårsvurdering))

fun fagsakpersoner(identer: Set<String>): Set<FagsakPerson> = identer.map {
    FagsakPerson(ident = it)
}.toSet()