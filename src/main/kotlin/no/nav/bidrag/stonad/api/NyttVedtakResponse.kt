package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Respons ved opprettelse av et stonad")
data class NyttstonadResponse(

  @ApiModelProperty(value = "Id til stonadet som er opprettet")
  val stonadId: Int = 0
)
