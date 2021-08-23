package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.dto.PeriodeDto
import java.time.LocalDateTime

data class FinnStonadResponse(

  @Schema(description = "StønadId")
  val stonadId: Int = 0,

  @Schema(description = "Stønadstype")
  val stonadType: String = "",

  @Schema(description = "Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @Schema(description = "opprettet_av")
  val opprettetAvSaksbehandlerId: String = "",

  @Schema(description = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "endret_av")
  val endretAvSaksbehandlerId: String? = "",

  @Schema(description = "når_sist_endret_timestamp")
  val endretTimestamp: LocalDateTime? = null,

  @Schema(description = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<PeriodeDto> = emptyList()
)