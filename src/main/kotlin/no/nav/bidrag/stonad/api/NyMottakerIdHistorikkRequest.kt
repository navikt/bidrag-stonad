package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Request for å endre mottaker-id på en stønad")
data class NyMottakerIdHistorikkRequest(

  @ApiModelProperty(value = "Stønad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Mottaker-id som skal erstattes")
  val mottakerIdEndretFra: String = "",

  @ApiModelProperty(value = "Mottaker-id som skal erstatte eksisterende id")
  val mottakerIdEndretTil: String = "",

  @ApiModelProperty(value = "opprettet_av")
  val saksbehandlerId: String = ""

)

fun NyMottakerIdHistorikkRequest.toMottakerIdHistorikkDto() = with(::MottakerIdHistorikkDto) {
  val propertiesByName = NyMottakerIdHistorikkRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkDto)
    }
  })
}
