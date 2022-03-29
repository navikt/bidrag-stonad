package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.bo.PeriodeBo
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en periode")
data class OpprettPeriodeRequest(

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

fun OpprettPeriodeRequest.toPeriodeBo(stonadId: Int) = with(::PeriodeBo) {
  val propertiesByName = OpprettPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeBo::stonadId.name -> stonadId
      PeriodeBo::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
    }
  })
}

fun OpprettPeriodeRequest.toPeriodeBo() = with(::PeriodeBo) {
  val propertiesByName = OpprettPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeBo::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
    }
  })
}