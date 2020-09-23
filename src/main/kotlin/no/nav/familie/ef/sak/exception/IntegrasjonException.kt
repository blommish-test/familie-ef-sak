package no.nav.familie.ef.sak.exception

import java.net.URI

class IntegrasjonException(msg: String,
                           throwable: Throwable? = null,
                           val uri: URI? = null,
                           val data: Any? = null) : RuntimeException(msg, throwable)
