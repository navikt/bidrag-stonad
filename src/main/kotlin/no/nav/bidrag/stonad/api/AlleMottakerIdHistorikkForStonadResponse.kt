package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto

@Schema
data class AlleMottakerIdHistorikkForStonadResponse(

  @Schema(description = "Alle forekomster på mottaker-id-historikk for en stønad")
  val alleMottakerIdHistorikkForStonad: List<MottakerIdHistorikkDto>? = emptyList()
)
