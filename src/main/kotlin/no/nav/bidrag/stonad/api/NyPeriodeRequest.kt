package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en periode")
data class NyPeriodeRequest(

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFom: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  val periodeTil: LocalDate? = null,

  @Schema(description = "Stonad-id")
  val stonadId: Int = 0,

  @Schema(description = "Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description = "Periode gjort ugyldig av vedtak-id")
  val periodeGjortUgyldigAvVedtakId: Int? = 0,

  @Schema(description = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description = "Resultatkoden tilhørende stønadsbeløpet")
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