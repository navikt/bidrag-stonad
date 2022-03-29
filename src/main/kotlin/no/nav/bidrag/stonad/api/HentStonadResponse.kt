package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.bo.PeriodeBo
import java.time.LocalDateTime

data class HentStonadResponse(

  @Schema(description = "StønadId")
  val stonadId: Int = 0,

  @Schema(description = "Stønadstype")
  val stonadType: StonadType,

  @Schema(description = "Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String,

  @Schema(description = "opprettet_av")
  val opprettetAv: String,

  @Schema(description = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime,

  @Schema(description = "endret_av")
  val endretAv: String? = null,

  @Schema(description = "når_sist_endret_timestamp")
  val endretTimestamp: LocalDateTime? = null,

  @Schema(description = "Liste over alle perioder som inngår i stønaden")
  val periodeListe: List<PeriodeBo>
)