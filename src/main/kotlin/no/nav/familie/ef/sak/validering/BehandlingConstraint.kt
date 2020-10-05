package no.nav.familie.ef.sak.validering

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Suppress("unused")
@MustBeDocumented
@Constraint(validatedBy = [Behandlingstilgang::class])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class BehandlingConstraint(val message: String = "Ikke tilgang til behandling",
                                      val groups: Array<KClass<*>> = [],
                                      val payload: Array<KClass<out Payload>> = [])
