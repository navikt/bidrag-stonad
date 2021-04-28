package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.PeriodeDto
import java.time.LocalDateTime

data class FinnStonadResponse(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "opprettet_av")
  val opprettetAvSaksbehandlerId: String = "",

  @ApiModelProperty(value = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "endret_av")
  val endretAvSaksbehandlerId: String? = "",

  @ApiModelProperty(value = "når_sist_endret_timestamp")
  val endretTimestamp: LocalDateTime? = null,

  @ApiModelProperty(value = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<PeriodeDto> = emptyList()
)