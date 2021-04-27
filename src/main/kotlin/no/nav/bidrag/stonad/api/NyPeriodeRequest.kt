package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved en periode")
data class NyPeriodeRequest(

  @ApiModelProperty(value = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @ApiModelProperty(value = "Stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Periode gjort ugyldig av vedtak-id")
  val periodeGjortUgyldigAvVedtakId: Int = 0,

  @ApiModelProperty(value = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende stønadsbeløpet")
  val resultatkode: String = "",

  )

fun NyPeriodeRequest.toPeriodeDto(stonadId: Int) = with(::PeriodeDto) {
  val propertiesByName = NyPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeDto::stonadId.name -> stonadId
      PeriodeDto::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}

fun NyPeriodeRequest.toPeriodeDto() = with(::PeriodeDto) {
  val propertiesByName = NyPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeDto::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}