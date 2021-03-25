package no.nav.familie.ef.sak.repository

import no.nav.familie.ef.sak.OppslagSpringRunnerTest
import no.nav.familie.ef.sak.no.nav.familie.ef.sak.repository.behandling
import no.nav.familie.ef.sak.no.nav.familie.ef.sak.repository.fagsak
import no.nav.familie.ef.sak.repository.domain.BehandlingStatus
import no.nav.familie.ef.sak.repository.domain.FagsakPerson
import no.nav.familie.ef.sak.repository.domain.Sporbar
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

internal class BehandlingRepositoryTest : OppslagSpringRunnerTest() {

    @Autowired private lateinit var fagsakRepository: FagsakRepository
    @Autowired private lateinit var behandlingRepository: BehandlingRepository

    @Test
    internal fun findByFagsakId() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))

        assertThat(behandlingRepository.findByFagsakId(UUID.randomUUID())).isEmpty()
        assertThat(behandlingRepository.findByFagsakId(fagsak.id)).containsOnly(behandling)
    }

    @Test
    internal fun findByFagsakIdAndAktivIsTrue() {
        val fagsak = fagsakRepository.insert(fagsak())
        behandlingRepository.insert(behandling(fagsak, aktiv = false))

        assertThat(behandlingRepository.findByFagsakIdAndAktivIsTrue(UUID.randomUUID())).isNull()
        assertThat(behandlingRepository.findByFagsakIdAndAktivIsTrue(fagsak.id)).isNull()

        val aktivBehandling = behandlingRepository.insert(behandling(fagsak, aktiv = true))
        assertThat(behandlingRepository.findByFagsakIdAndAktivIsTrue(fagsak.id)).isEqualTo(aktivBehandling)
    }

    @Test
    internal fun findByFagsakAndStatus() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak, status = BehandlingStatus.OPPRETTET))

        assertThat(behandlingRepository.findByFagsakIdAndStatus(UUID.randomUUID(), BehandlingStatus.OPPRETTET)).isEmpty()
        assertThat(behandlingRepository.findByFagsakIdAndStatus(fagsak.id, BehandlingStatus.FERDIGSTILT)).isEmpty()
        assertThat(behandlingRepository.findByFagsakIdAndStatus(fagsak.id, BehandlingStatus.OPPRETTET)).containsOnly(behandling)
    }

    @Test
    internal fun finnMedEksternId() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))
        val findByBehandlingId = behandlingRepository.findById(behandling.id)
        val findByEksternId = behandlingRepository.finnMedEksternId(behandling.eksternId.id)
                              ?: throw error("Behandling med id ${behandling.eksternId.id} finnes ikke")

        assertThat(findByEksternId).isEqualTo(behandling)
        assertThat(findByEksternId).isEqualTo(findByBehandlingId.get())
    }

    @Test
    internal fun `finnFnrForBehandlingId(sql) skal finne gjeldende fnr for behandlingsid`() {
        val fagsak = fagsakRepository.insert(fagsak(setOf(FagsakPerson(ident = "1"),
                                                          FagsakPerson(ident = "2", sporbar = Sporbar(opprettetTid = LocalDateTime.now().plusDays(2))),
                                                          FagsakPerson(ident = "3"))))
        val behandling = behandlingRepository.insert(behandling(fagsak))
        val fnr = behandlingRepository.finnAktivIdent(behandling.id)
        assertThat(fnr).isEqualTo("2")
    }

    @Test
    internal fun `finnMedEksternId skal gi null når det ikke finnes behandling for gitt id`() {
        val findByEksternId = behandlingRepository.finnMedEksternId(1000000L)
        assertThat(findByEksternId).isEqualTo(null)
    }

    @Test
    internal fun `finnStatus - skal returnere status`() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))
        assertThat(behandlingRepository.finnStatus(behandling.id)).isEqualTo(behandling.status)
    }

    @Test
    internal fun `oppdaterStatus - skal oppdatere status på behandling`() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))
        val harOppdatertStatus =
                behandlingRepository.oppdaterStatus(behandling.id, BehandlingStatus.FERDIGSTILT, behandling.status)

        val oppdatertBehandling = behandlingRepository.findByIdOrThrow(behandling.id)

        assertThat(harOppdatertStatus).isTrue
        assertThat(behandling.status).isNotEqualTo(BehandlingStatus.FERDIGSTILT)
        assertThat(oppdatertBehandling.status).isEqualTo(BehandlingStatus.FERDIGSTILT)
    }

    @Test
    internal fun `oppdaterStatus - skal returnere false`() {
        val harOppdatertStatus = behandlingRepository.oppdaterStatus(behandlingId = UUID.randomUUID(),
                                                                     status = BehandlingStatus.FERDIGSTILT,
                                                                     tidligereStatus = BehandlingStatus.FERDIGSTILT)
        assertThat(harOppdatertStatus).isFalse
    }

    @Test
    internal fun `skal kaste feil hvis man prøver å oppdatere en behandling etter att man oppdatert status som oppdaterer versjon`() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))
        behandlingRepository.oppdaterStatus(behandling.id, BehandlingStatus.FERDIGSTILT, behandling.status)
        assertThat(catchThrowable { behandlingRepository.update(behandling) })
                .hasRootCauseMessage("Optimistic lock exception on saving entity of type no.nav.familie.ef.sak.repository.domain.Behandling.")
    }
}