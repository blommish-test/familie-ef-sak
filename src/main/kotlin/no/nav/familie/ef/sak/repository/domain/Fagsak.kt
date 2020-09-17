package no.nav.familie.ef.sak.repository.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("fagsak")
data class Fagsak(@Id
                  val id: UUID = UUID.randomUUID(),
                  @Column("stonadstype")
                  val stønadstype: Stønadstype,
                  @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
                  val sporbar: Sporbar = Sporbar())

enum class Stønadstype {
    OVERGANGSSTØNAD,
    BARNETILSYN,
    SKOLEPENGER
}

