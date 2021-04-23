package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.stonadDto

@ApiModel
data class AllestonadResponse(

  @ApiModelProperty(value = "Alle stonad")
  val allestonad: List<stonadDto> = emptyList()
)
