package no.nav.familie.ef.sak.domene

enum class DokumentBrevkode(val verdi: String) {
    OVERGANGSSTØNAD("NAV 15-00.01"),
    BARNETILSYN("NAV 15-00.02"),
    SKOLEPENGER("NAV 15-00.04");

    companion object {
        fun erGyldigBrevkode(brevKode: String?): Boolean = values().any { it.verdi === brevKode}
        fun fraBrevkode(brevKode: String?): DokumentBrevkode = values().first { it.verdi === brevKode}
    }

}