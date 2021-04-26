package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Egenskaper ved en stonad")
data class NyKomplettstonadRequest(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "behandlingId, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "Liste over alle perioder som inngår i stonaden")
  val periodeListe: List<NyPeriodeRequest> = emptyList()
)

fun NyKomplettstonadRequest.toStonadDto(vedtakId: Int) = with(::StonadDto) {
  val propertiesByName = NyStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::vedtakId.name -> vedtakId
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}