package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.StonadDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved en stønadsendring")
data class NyStonadRequest(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "opprettet_av")
  val opprettetAvSaksbehandlerId: String = "",

  @ApiModelProperty(value = "endret_av")
  val endretAvSaksbehandlerId: String = "",

  @ApiModelProperty(value = "Liste over alle perioder som inngår i stønaden")
  val periodeListe: List<NyPeriodeRequest> = emptyList()
)

fun NyStonadRequest.toStonadDto(stonadId: Int) = with(::StonadDto) {
  val propertiesByName = NyStonadRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadDto::stonadId.name -> stonadId
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}

fun NyStonadRequest.toStonadDto() = with(::StonadDto) {
  val propertiesByName = NyStonadRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadDto::stonadId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}