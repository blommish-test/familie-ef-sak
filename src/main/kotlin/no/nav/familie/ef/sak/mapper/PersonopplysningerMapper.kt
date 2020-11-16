package no.nav.familie.ef.sak.mapper

import no.nav.familie.ef.sak.api.dto.*
import no.nav.familie.ef.sak.api.dto.Adressebeskyttelse
import no.nav.familie.ef.sak.api.dto.Folkeregisterpersonstatus
import no.nav.familie.ef.sak.api.dto.Kjønn
import no.nav.familie.ef.sak.api.dto.Sivilstandstype
import no.nav.familie.ef.sak.domene.SøkerMedBarn
import no.nav.familie.ef.sak.integration.dto.pdl.*
import no.nav.familie.ef.sak.service.ArbeidsfordelingService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PersonopplysningerMapper(private val adresseMapper: AdresseMapper,
                               private val statsborgerskapMapper: StatsborgerskapMapper,
                               private val arbeidsfordelingService: ArbeidsfordelingService) {

    fun tilPersonopplysninger(personMedRelasjoner: SøkerMedBarn,
                              ident: String,
                              fullmakter: List<Fullmakt>,
                              egenAnsatt: Boolean,
                              identNavn: Map<String, String>): PersonopplysningerDto {
        val søker = personMedRelasjoner.søker
        return PersonopplysningerDto(
                adressebeskyttelse = søker.adressebeskyttelse.firstOrNull()
                        ?.let { Adressebeskyttelse.valueOf(it.gradering.name) },
                folkeregisterpersonstatus = søker.folkeregisterpersonstatus.firstOrNull()
                        ?.let { Folkeregisterpersonstatus.fraPdl(it) },
                dødsdato = søker.dødsfall.firstOrNull()?.dødsdato,
                navn = NavnDto.fraNavn(søker.navn.gjeldende()),
                kjønn = søker.kjønn.single().kjønn.let { Kjønn.valueOf(it.name) },
                personIdent = ident,
                telefonnummer = søker.telefonnummer.find { it.prioritet == 1 }
                        ?.let { TelefonnummerDto(it.landskode, it.nummer) },
                statsborgerskap = statsborgerskapMapper.map(søker.statsborgerskap),
                sivilstand = søker.sivilstand.map {
                    SivilstandDto(type = Sivilstandstype.valueOf(it.type.name),
                                  gyldigFraOgMed = it.gyldigFraOgMed?.toString() ?: it.bekreftelsesdato,
                                  relatertVedSivilstand = it.relatertVedSivilstand,
                                  navn = identNavn[it.relatertVedSivilstand])
                },
                adresse = tilAdresser(søker),
                fullmakt = fullmakter.map {
                    FullmaktDto(gyldigFraOgMed = it.gyldigFraOgMed,
                                gyldigTilOgMed = it.gyldigTilOgMed,
                                motpartsPersonident = it.motpartsPersonident,
                                navn = identNavn[it.motpartsPersonident])
                },
                egenAnsatt = egenAnsatt,
                navEnhet = arbeidsfordelingService.hentNavEnhet(ident)
                                   ?.let { it.enhetId + " - " + it.enhetNavn } ?: "Ikke funnet",
                barn = personMedRelasjoner.barn.map {
                    mapBarn(
                            it.key,
                            it.value,
                            personMedRelasjoner.søkerIdent,
                            personMedRelasjoner.søker.bostedsadresse,
                            identNavn)
                },
                innflyttingTilNorge = søker.innflyttingTilNorge,
                utflyttingFraNorge = søker.utflyttingFraNorge,
                )
    }

    fun tilAdresser(søker: PdlSøker): List<AdresseDto> {
        val adresser =
                søker.bostedsadresse.map(adresseMapper::tilAdresse) +
                søker.kontaktadresse.map(adresseMapper::tilAdresse) +
                søker.oppholdsadresse.map(adresseMapper::tilAdresse)
        return adresser.sortedWith(compareByDescending<AdresseDto>
                                   { it.gyldigFraOgMed ?: LocalDate.MAX }
                                           .thenBy(AdresseDto::type))
    }

    fun mapBarn(personIdent: String,
                pdlBarn: PdlBarn,
                søkerIdent: String,
                bostedsadresserForelder: List<Bostedsadresse>,
                identNavnMap: Map<String, String>): BarnDto {

        val annenForelderIdent = pdlBarn.familierelasjoner.find {
            it.relatertPersonsIdent != søkerIdent && it.relatertPersonsRolle != Familierelasjonsrolle.BARN
        }?.relatertPersonsIdent
        return BarnDto(
                personIdent = personIdent,
                navn = pdlBarn.navn.gjeldende().visningsnavn(),
                annenForelder = annenForelderIdent?.let { AnnenForelderDTO(it, identNavnMap[it] ?: "Finner ikke navn") },
                adresse = pdlBarn.bostedsadresse.map(adresseMapper::tilAdresse),
                borHosSøker = borPåSammeAdresse(pdlBarn.bostedsadresse, bostedsadresserForelder)
        )
    }

    fun borPåSammeAdresse(bostedsadresserForelder: List<Bostedsadresse>, bostedsadresserBarn: List<Bostedsadresse>): Boolean {

        val gjeldendeBostedsadresseBarn = finnGjeldendeBostedsadresse(bostedsadresserBarn)
        val gjeldendeBostedsadresseForelder = finnGjeldendeBostedsadresse(bostedsadresserForelder)
        return gjeldendeBostedsadresseBarn?.let {
            return it.vegadresse == gjeldendeBostedsadresseForelder?.vegadresse
        } ?: false
    }

    private fun finnGjeldendeBostedsadresse(bostedsadresser: List<Bostedsadresse>): Bostedsadresse? {
        val bostedsadresse = bostedsadresser
                .filter { it.folkeregistermetadata.gyldighetstidspunkt != null && it.folkeregistermetadata.opphørstidspunkt == null }
                .maxByOrNull { it.folkeregistermetadata.gyldighetstidspunkt!! }
        return bostedsadresse ?: bostedsadresser.firstOrNull()
    }

}