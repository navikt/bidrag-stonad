package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.stonadDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class stonad (

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonad_id")
  val stonadId: Int = 0,

  @Column(nullable = false, name = "enhet_id")
  val enhetId: String = "",

  @Column(nullable = false, name = "opprettet_av")
  val saksbehandlerId: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun stonad.tostonadDto() = with(::stonadDto) {
  val propertiesByName = stonad::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@tostonadDto)
    }
  })
}
