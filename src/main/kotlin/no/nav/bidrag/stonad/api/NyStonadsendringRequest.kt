package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.StonadsendringDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved en stønadsendring")
data class NyStonadsendringRequest(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "stonad-id")
  val stonadId: Int = 0,

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

  @ApiModelProperty(value = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<NyPeriodeRequest> = emptyList()
)

fun NyStonadsendringRequest.toStonadsendringDto(stonadId: Int) = with(::StonadsendringDto) {
  val propertiesByName = NyStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::stonadId.name -> stonadId
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}

fun NyStonadsendringRequest.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = NyStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}

