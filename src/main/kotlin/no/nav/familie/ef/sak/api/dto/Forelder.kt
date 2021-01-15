package no.nav.familie.ef.sak.api.dto

import no.nav.familie.kontrakter.ef.søknad.EnumTekstverdiMedSvarId
import java.time.LocalDate

data class Forelder(val fødselsnummerAnnenForelder: String?,
                    val navn: String?,
                    val fødselsdato: LocalDate?,
                    val bostedsland: String,
                    val harForeldreneBoddSammen: Boolean?,
                    val fraflyttingsdato: LocalDate?,
                    val foreldresKontakt: String?,
                    val næreBoforhold: EnumTekstverdiMedSvarId?,
                    val kanSøkerAnsesÅHaAleneomsorgen: Boolean?,
                    val aleneomsorgBegrunnelse: String?,
                    val adresser: List<Adresse>?)