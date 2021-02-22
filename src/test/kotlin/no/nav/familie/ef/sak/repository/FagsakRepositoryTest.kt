package no.nav.familie.ef.sak.no.nav.familie.ef.sak.repository

import no.nav.familie.ef.sak.OppslagSpringRunnerTest
import no.nav.familie.ef.sak.repository.FagsakRepository
import no.nav.familie.ef.sak.repository.domain.Stønadstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

internal class FagsakRepositoryTest : OppslagSpringRunnerTest() {

    @Autowired private lateinit var fagsakRepository: FagsakRepository

    @Test
    internal fun findByFagsakId() {
        val fagsakPersistert = fagsakRepository.insert(fagsak(fagsakpersoner(setOf("12345678901", "98765432109"))))
        val fagsak = fagsakRepository.findByIdOrNull(fagsakPersistert.id) ?: error("Finner ikke fagsak med id")

        assertThat(fagsak).isNotNull
        assertThat(fagsak.søkerIdenter).isNotEmpty
        assertThat(fagsak.søkerIdenter.map { it.ident }).contains("12345678901")
        assertThat(fagsak.søkerIdenter.map { it.ident }).contains("98765432109")
    }

    @Test
    internal fun findBySøkerIdent() {
        fagsakRepository.insert(fagsak(fagsakpersoner(setOf("12345678901", "98765432109"))))
        val fagsakHentetFinnesIkke = fagsakRepository.findBySøkerIdent("0", Stønadstype.OVERGANGSSTØNAD)

        assertThat(fagsakHentetFinnesIkke).isNull()

        val fagsak = fagsakRepository.findBySøkerIdent("12345678901", Stønadstype.OVERGANGSSTØNAD) ?: error("Finner ikke fagsak")

        assertThat(fagsak.søkerIdenter.map { it.ident }).contains("12345678901")
        assertThat(fagsak.søkerIdenter.map { it.ident }).contains("98765432109")
    }


    @Test
    internal fun `skal returnere en liste med fagsaker hvis stønadstypen ikke satt`() {
        val fagsakPerson = fagsakpersoner(setOf("12345678901"))
        fagsakRepository.insert(fagsak(identer = fagsakPerson, stønadstype = Stønadstype.OVERGANGSSTØNAD))
        fagsakRepository.insert(fagsak(identer = fagsakPerson, stønadstype = Stønadstype.SKOLEPENGER))
        val fagsaker = fagsakRepository.findBySøkerIdent("12345678901")

        assertThat(fagsaker.forEach { fagsak ->
                assertThat(fagsak.søkerIdenter.size).isEqualTo(1)
                assertThat(fagsak.søkerIdenter.map { it.ident }).contains("12345678901")
        })


        assertThat(fagsaker.map { it.stønadstype }).contains(Stønadstype.SKOLEPENGER)
        assertThat(fagsaker.map { it.stønadstype }).contains(Stønadstype.OVERGANGSSTØNAD)
    }

    @Test
    internal fun finnMedEksternId() {
        val fagsak = fagsakRepository.insert(fagsak())
        val findByFagsakId = fagsakRepository.findById(fagsak.id)
        val findByEksternId = fagsakRepository.finnMedEksternId(fagsak.eksternId.id)
                              ?: throw error("Fagsak med ekstern id ${fagsak.eksternId} finnes ikke")

        assertThat(findByEksternId).isEqualTo(fagsak)
        assertThat(findByEksternId).isEqualTo(findByFagsakId.get())
    }

    @Test
    internal fun `finnMedEksternId skal gi null når det ikke finnes fagsak for gitt id`() {
        val findByEksternId = fagsakRepository.finnMedEksternId(100000L)
        assertThat(findByEksternId).isEqualTo(null)
    }

}