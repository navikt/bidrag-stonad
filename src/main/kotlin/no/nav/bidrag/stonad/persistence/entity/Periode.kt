package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.stonad.bo.PeriodeBo
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Periode(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "periode_id")
  val periodeId: Int = 0,

  @Column(nullable = false, name = "periode_fom")
  val periodeFom: LocalDate = LocalDate.now(),

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = null,

  @ManyToOne
  @JoinColumn(name = "stonad_id")
  val stonad: Stonad = Stonad(),

  @Column(name = "vedtak_id")
  val vedtakId: Int = 0,

  @Column(nullable = true, name = "periode_gjort_ugyldig_av_vedtak_id")
  val periodeGjortUgyldigAvVedtakId: Int? = null,

  @Column(nullable = true, name = "belop")
  val belop: BigDecimal? = null,

  @Column(nullable = true, name = "valutakode")
  val valutakode: String? = null,

  @Column(nullable = false, name = "resultatkode")
  val resultatkode: String = ""
)

fun OpprettStonadPeriodeRequestDto.toPeriodeEntity(eksisterendeStonad: Stonad) = with(::Periode) {
  val propertiesByName = OpprettStonadPeriodeRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Periode::stonad.name -> eksisterendeStonad
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}

  fun Periode.toStonadPeriodeDto() = with(::StonadPeriodeDto) {
    val propertiesByName = Periode::class.memberProperties.associateBy { it.name }
    callBy(parameters.associateWith { parameter ->
      when (parameter.name) {
        PeriodeBo::stonadId.name -> stonad.stonadId
        else -> propertiesByName[parameter.name]?.get(this@toStonadPeriodeDto)
      }
    })
}