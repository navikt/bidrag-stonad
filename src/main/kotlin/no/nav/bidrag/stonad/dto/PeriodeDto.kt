package no.nav.bidrag.stonad.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class PeriodeDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @ApiModelProperty(value = "Stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Periode-gjort-ugyldig-av-Vedtak-id")
  val periodeGjortUgyldigAvVedtakId: Int? = 0,

  @ApiModelProperty(value = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende stønadsbeløpet")
  val resultatkode: String = ""
)

fun PeriodeDto.toPeriodeEntity(eksisterendeStonad: Stonad) = with(::Periode) {
  val propertiesByName = PeriodeDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Periode::stonad.name -> eksisterendeStonad
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}
