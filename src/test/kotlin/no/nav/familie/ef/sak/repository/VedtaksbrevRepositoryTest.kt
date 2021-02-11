package no.nav.familie.ef.sak.no.nav.familie.ef.sak.repository

import no.nav.familie.ef.sak.OppslagSpringRunnerTest
import no.nav.familie.ef.sak.api.dto.BrevRequest
import no.nav.familie.ef.sak.repository.BehandlingRepository
import no.nav.familie.ef.sak.repository.FagsakRepository
import no.nav.familie.ef.sak.repository.VedtaksbrevRepository
import no.nav.familie.ef.sak.repository.domain.Vedtaksbrev
import no.nav.familie.ef.sak.service.steg.StegType
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

internal class VedtaksbrevRepositoryTest : OppslagSpringRunnerTest() {

    @Autowired private lateinit var vedtaksbrevRepository: VedtaksbrevRepository
    @Autowired private lateinit var fagsakRepository: FagsakRepository
    @Autowired private lateinit var behandlingRepository: BehandlingRepository

    @Test
    internal fun findByBehandlingId() {
        val fagsak = fagsakRepository.insert(fagsak())
        val behandling = behandlingRepository.insert(behandling(fagsak))
        val vedtaksbrev = Vedtaksbrev(behandlingId = behandling.id,
                                      steg = StegType.SEND_TIL_BESLUTTER,
                                      brevRequest = BrevRequest("Olav Olavssen", "12345678910", LocalDate.now(), LocalDate.now(), "fordi jepp", LocalDate.now(), 1300),
                                      pdf = ByteArray(123))

        vedtaksbrevRepository.insert(vedtaksbrev)

        assertThat(vedtaksbrevRepository.findByBehandlingId(behandling.id)).first().isEqualToComparingFieldByField(vedtaksbrev)
    }
}