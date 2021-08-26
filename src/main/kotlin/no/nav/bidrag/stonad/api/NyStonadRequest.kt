package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.dto.StonadDto
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en stønadsendring")
data class NyStonadRequest(

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

  @Schema(description = "endret_av")
  val endretAvSaksbehandlerId: String? = null,

  @Schema(description = "Liste over alle perioder som inngår i stønaden")
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