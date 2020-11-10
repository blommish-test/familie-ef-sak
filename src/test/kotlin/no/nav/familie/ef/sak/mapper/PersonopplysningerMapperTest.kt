package no.nav.familie.ef.sak.mapper

import io.mockk.mockk
import no.nav.familie.ef.sak.integration.dto.pdl.Bostedsadresse
import no.nav.familie.ef.sak.integration.dto.pdl.Folkeregistermetadata
import no.nav.familie.ef.sak.integration.dto.pdl.Vegadresse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalDateTime.now

internal class PersonopplysningerMapperTest {

    val personopplysningerMapper = PersonopplysningerMapper(mockk(), mockk(), mockk())

    fun adresseOslo() = Vegadresse("1", "ABC", "123", "Oslogata", "01", null, "0101", null)
    fun adresseTrondheim() = Vegadresse("1", "ABC", "123", "Trøndergata", "01", null, "7080", null)
    fun adresseTromsø() = Vegadresse("1", "ABC", "123", "Tromsøygata", "01", null, "9099", null)
    fun adresseBergen() = Vegadresse("1", "ABC", "123", "Bergensgata", "01", null, "5020", null)


    @Test
    internal fun `forelder og barn bor på samme adresse`() {

        val barnAdresser = listOf(
                lagAdresse(adresseBergen(), now().minusDays(100)),
                lagAdresse(adresseTromsø(), now().minusDays(1)),
                lagAdresse(adresseTrondheim(), null)
        )
        val forelderAdresser = listOf(
                lagAdresse(adresseOslo(), now().minusDays(1000), now().minusDays(100)),
                lagAdresse(adresseBergen(), now().minusDays(100), now().minusDays(1)),
                lagAdresse(adresseTromsø(), now().minusDays(1))
        )
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(barnAdresser, forelderAdresser)).isTrue
    }

    @Test
    internal fun `forelder og barn bor ikke på samme adresse`() {

        val barn1Adresser = listOf(
                lagAdresse(adresseBergen(), now().minusDays(1)),
                lagAdresse(adresseTromsø(), now().minusDays(100))
        )
        val barn2Adresser = listOf(
                lagAdresse(adresseBergen(), now().minusDays(1)),
                lagAdresse(adresseTromsø(), null)
        )

        val ugyldigeAdresser = listOf(
                lagAdresse(null, null)
        )

        val forelderAdresser = listOf(
                lagAdresse(adresseOslo(), now().minusDays(1000), now().minusDays(100)),
                lagAdresse(adresseBergen(), now().minusDays(100), now().minusDays(1)),
                lagAdresse(adresseTromsø(), now().minusDays(1))
        )
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(barn1Adresser, forelderAdresser)).isFalse
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(barn2Adresser, forelderAdresser)).isFalse
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(ugyldigeAdresser, forelderAdresser)).isFalse
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(emptyList(), forelderAdresser)).isFalse
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(emptyList(), emptyList())).isFalse
    }

    @Test
    internal fun `forelder og barn bor på samme adresse selv om det ikke finnes gyldighetsdato`() {

        val barnAdresser = listOf(lagAdresse(adresseTromsø(), null), lagAdresse(adresseOslo(), null))
        val forelderAdresser = listOf(
                lagAdresse(adresseOslo(), now().minusDays(1000), now().minusDays(100)),
                lagAdresse(adresseBergen(), now().minusDays(100), now().minusDays(1)),
                lagAdresse(adresseTromsø(), now().minusDays(1))
        )
        Assertions.assertThat(personopplysningerMapper.borPåSammeAdresse(barnAdresser, forelderAdresser)).isTrue()
    }


    fun lagAdresse(vegadresse: Vegadresse?,
                   gyldighetstidspunkt: LocalDateTime?,
                   opphørstidspunkt: LocalDateTime? = null): Bostedsadresse {
        return Bostedsadresse(
                vegadresse = vegadresse,
                angittFlyttedato = null,
                coAdressenavn = null,
                folkeregistermetadata = Folkeregistermetadata(
                        gyldighetstidspunkt = gyldighetstidspunkt,
                        opphørstidspunkt = opphørstidspunkt),
                utenlandskAdresse = null,
                ukjentBosted = null
        )
    }
}