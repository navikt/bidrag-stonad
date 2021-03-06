package no.nav.bidrag.stonad.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class PeriodeBo(

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

fun OpprettStonadPeriodeRequestDto.toPeriodeBo() = with(::PeriodeBo) {
  val propertiesByName = OpprettStonadPeriodeRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeBo::periodeId.name -> 0
      PeriodeBo::stonadId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
    }
  })
}


fun StonadPeriodeDto.toPeriodeBo() = with(::PeriodeBo) {
  val propertiesByName = StonadPeriodeDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeBo::stonadId.name -> stonadId
      PeriodeBo::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
    }
  })
}

fun PeriodeBo.toPeriodeEntity(eksisterendeStonad: Stonad) = with(::Periode) {
  val propertiesByName = PeriodeBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Periode::stonad.name -> eksisterendeStonad
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}
