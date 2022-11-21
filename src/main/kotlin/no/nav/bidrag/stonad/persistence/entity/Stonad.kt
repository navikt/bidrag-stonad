package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Stonad(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonad_id")
  val stonadId: Int = 0,

  @Column(nullable = false, name = "stonad_type")
  val stonadType: String = "",

  @Column(nullable = false, name = "sak_id")
  val sakId: String = "",

  @Column(nullable = false, name = "skyldner_id")
  val skyldnerId: String = "",

  @Column(nullable = false, name = "kravhaver_id")
  val kravhaverId: String = "",

  @Column(nullable = false, name = "mottaker_id")
  val mottakerId: String = "",

  @Column(nullable = true, name = "indeksregulering_aar")
  val indeksreguleringAar: String? = "",

  @Column(nullable = true, name = "opphortFra")
  val opphortFra: LocalDate? = null,

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAv: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "endret_av")
  val endretAv: String? = null,

  @Column(nullable = true, name = "endret_timestamp")
  val endretTimestamp: LocalDateTime? = null
)

fun OpprettStonadRequestDto.toStonadEntity() = with(::Stonad) {
  val propertiesByName = OpprettStonadRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Stonad::stonadId.name -> 0
      Stonad::stonadType.name -> stonadType.toString()
      Stonad::opprettetTimestamp.name -> LocalDateTime.now()
      Stonad::endretAv.name -> opprettetAv
      else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
    }
  })
}

fun Stonad.toStonadDto() = with(::StonadDto) {
  val propertiesByName = Stonad::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}
