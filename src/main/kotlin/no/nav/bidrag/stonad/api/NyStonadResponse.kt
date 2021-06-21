package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description ="Respons ved opprettelse av en stonad")
data class NyStonadResponse(

  @Schema(description = "Id til stonaden som er opprettet")
  val stonadId: Int = 0
)
