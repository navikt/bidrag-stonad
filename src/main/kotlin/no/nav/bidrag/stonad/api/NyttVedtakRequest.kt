package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Egenskaper ved et stonad")
data class NyttstonadRequest(

  @ApiModelProperty(value = "Id til saksbehandler som oppretter stonadet")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for stonadet")
  val enhetId: String = "",

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i stonadet")
  val grunnlagListe: List<NyttGrunnlagRequest> = emptyList(),

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i stonadet")
  val stonadsendringListe: List<NyStonadsendringRequest> = emptyList()
)