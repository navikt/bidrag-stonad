package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.StonadsendringDto

@ApiModel
data class AlleStonadsendringerForstonadResponse(

  @ApiModelProperty(value = "Alle stønadsendringer for et stonad")
  val alleStonadsendringerForstonad: List<StonadsendringDto> = emptyList()
)
