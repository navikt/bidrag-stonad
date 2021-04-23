package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.PeriodeDto

@ApiModel
data class AllePerioderForStonadsendringResponse(

  @ApiModelProperty(value = "Alle perioder for en stonadsendring")
  val allePerioderForStonadsendring: List<PeriodeDto> = emptyList()
)
