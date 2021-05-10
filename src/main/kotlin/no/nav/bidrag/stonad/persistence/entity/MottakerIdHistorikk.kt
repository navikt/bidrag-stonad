package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@IdClass(MottakerIdHistorikkPK::class)
@Entity
data class MottakerIdHistorikk (

  @Id
  @ManyToOne
  @JoinColumn(name = "stonad_id")
  val stonad: Stonad = Stonad(),

  @Column(nullable = false, name = "mottaker_id_endret_fra")
  val mottakerIdEndretFra: String = "",

  @Column(nullable = false, name = "mottaker_id_endret_til")
  val mottakerIdEndretTil: String = "",

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
      MottakerIdHistorikkDto::stonadId.name -> stonad.stonadId
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkDto)
    }
  })
}

class MottakerIdHistorikkPK(val stonad: Int = 0, val opprettetTimestamp: LocalDateTime = LocalDateTime.now()) : Serializable