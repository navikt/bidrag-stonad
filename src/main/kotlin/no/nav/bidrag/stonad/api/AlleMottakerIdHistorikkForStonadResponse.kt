package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto

@ApiModel
data class AlleMottakerIdHistorikkForStonadResponse(

  @ApiModelProperty(value = "Alle forekomster på mottaker-id-historikk for en stønad")
  val alleMottakerIdHistorikkForStonad: List<MottakerIdHistorikkDto>? = emptyList()
)
