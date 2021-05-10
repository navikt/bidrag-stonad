package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Respons ved opprettelse av en stonad")
data class NyStonadResponse(

  @ApiModelProperty(value = "Id til stonaden som er opprettet")
  val stonadId: Int = 0
)
