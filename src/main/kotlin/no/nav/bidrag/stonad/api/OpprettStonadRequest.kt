package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.bo.StonadBo
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en stønadsendring")
data class OpprettStonadRequest(

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

  @Schema(description = "endret_av")
  val endretAv: String? = null,

  @Schema(description = "Liste over alle perioder som inngår i stønaden")
  val periodeListe: List<OpprettPeriodeRequest>
)

fun OpprettStonadRequest.toStonadDto(stonadId: Int) = with(::StonadBo) {
  val propertiesByName = OpprettStonadRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadBo::stonadId.name -> stonadId
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}

fun OpprettStonadRequest.toStonadDto() = with(::StonadBo) {
  val propertiesByName = OpprettStonadRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadBo::stonadId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}