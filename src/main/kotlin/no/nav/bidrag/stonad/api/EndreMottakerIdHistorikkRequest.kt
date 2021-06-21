package no.nav.bidrag.stonad.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import kotlin.reflect.full.memberProperties

@Schema(description ="Request for å endre mottaker-id på en stønad")
data class EndreMottakerIdHistorikkRequest(

  @Schema(description = "Stønad-id")
  val stonadId: Int = 0,

  @Schema(description = "Mottaker-id som skal erstattes")
  val mottakerIdEndretFra: String = "",

  @Schema(description = "Mottaker-id som skal erstatte eksisterende id")
  val mottakerIdEndretTil: String = "",

  @Schema(description = "opprettet_av")
  val saksbehandlerId: String = ""

)

fun EndreMottakerIdHistorikkRequest.toMottakerIdHistorikkDto() = with(::MottakerIdHistorikkDto) {
  val propertiesByName = EndreMottakerIdHistorikkRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkDto)
    }
  })
}
