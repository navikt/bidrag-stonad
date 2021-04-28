package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class MottakerIdHistorikk (

  @Id
  @ManyToOne
  @Column(name = "stonad_id")
  val stonad: Stonad = Stonad(),

  @Column(nullable = false, name = "enhet_id")
  val enhetId: String = "",

  @Column(nullable = false, name = "opprettet_av")
  val saksbehandlerId: String = "",

  @Id
  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun MottakerIdHistorikk.toMottakerIdHistorikkDto() = with(::MottakerIdHistorikkDto) {
  val propertiesByName = MottakerIdHistorikk::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkDto)
    }
  })
}
