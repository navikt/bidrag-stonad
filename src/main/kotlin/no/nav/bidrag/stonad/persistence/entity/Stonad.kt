package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.StonadDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Stonad(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonad_id")
  val stonadId: Int = 0,

  @Column(nullable = false, name = "stonad_type")
  val stonadType: String = "",

  @Column(nullable = true, name = "sak_id")
  val sakId: String? = null,

  @Column(nullable = false, name = "skyldner_id")
  val skyldnerId: String = "",

  @Column(nullable = false, name = "kravhaver_id")
  val kravhaverId: String = "",

  @Column(nullable = false, name = "mottaker_id")
  val mottakerId: String = "",

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAvSaksbehandlerId: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "endret_av")
  val endretAvSaksbehandlerId: String? = null,

  @Column(nullable = true, name = "endret_timestamp")
  val endretTimestamp: LocalDateTime? = null
)

fun Stonad.toStonadDto() = with(::StonadDto) {
  val propertiesByName = Stonad::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}
