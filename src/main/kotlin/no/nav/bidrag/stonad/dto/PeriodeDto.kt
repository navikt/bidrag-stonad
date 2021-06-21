package no.nav.bidrag.stonad.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class PeriodeDto(

  @Schema(description = "Periode-id")
  val periodeId: Int = 0,

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFom: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  val periodeTil: LocalDate? = null,

  @Schema(description = "Stonad-id")
  val stonadId: Int = 0,

  @Schema(description = "Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description = "Periode-gjort-ugyldig-av-vedtak-id")
  val periodeGjortUgyldigAvVedtakId: Int? = 0,

  @Schema(description = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description = "Resultatkode for stønaden")
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
