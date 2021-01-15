package no.nav.familie.ef.sak.repository.domain.søknad

import no.nav.familie.kontrakter.ef.søknad.EnumTekstverdiMedSvarId
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDate

data class Aktivitet(@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "hvordan_er_arbeidssituasjonen_")
                     val hvordanErArbeidssituasjonen: EnumTekstverdiMedSvarId,
                     @MappedCollection(idColumn = "soknadsskjema_id")
                     val arbeidsforhold: Set<Arbeidsgiver>? = emptySet(),
                     @MappedCollection(idColumn = "soknadsskjema_id")
                     val firmaer: Set<Selvstendig>? = emptySet(),
                     @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "virksomhet_")
                     val virksomhet: Virksomhet? = null,
                     @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "arbeidssoker_")
                     val arbeidssøker: Arbeidssøker? = null,
                     @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "under_utdanning_")
                     val underUtdanning: UnderUtdanning? = null,
                     @MappedCollection(idColumn = "soknadsskjema_id")
                     val aksjeselskap: Set<Aksjeselskap>? = emptySet(),
                     @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "er_i_arbeid_")
                     val erIArbeid: EnumTekstverdiMedSvarId? = null,
                     val erIArbeidDokumentasjon: Dokumentasjon? = null,
                     @MappedCollection(idColumn = "soknadsskjema_id")
                     val tidligereUtdanninger: Set<TidligereUtdanning> = emptySet()
)

data class Arbeidsgiver(val arbeidsgivernavn: String,
                        val arbeidsmengde: Int? = null,
                        @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "fast_eller_midlertidig_")
                        val fastEllerMidlertidig: EnumTekstverdiMedSvarId,
                        val harSluttdato: Boolean?,
                        val sluttdato: LocalDate? = null)

data class Selvstendig(val firmanavn: String,
                       val organisasjonsnummer: String,
                       val etableringsdato: LocalDate,
                       val arbeidsmengde: Int? = null,
                       val hvordanSerArbeidsukenUt: String)

data class Virksomhet(val virksomhetsbeskrivelse: String,
                      val dokumentasjon: Dokumentasjon? = null)

data class Aksjeselskap(val navn: String,
                        val arbeidsmengde: Int? = null)
