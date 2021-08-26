package no.nav.bidrag.stonad.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class StonadDto(

  @Schema(description = "Stønad-id")
  val stonadId: Int = 0,

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

  @Schema(description = "Id til saksbehandler som oppretter stønaden")
  val opprettetAvSaksbehandlerId: String = "",

  @Schema(description = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Id til saksbehandler som sist endret stønaden")
  val endretAvSaksbehandlerId: String? = null,

  @Schema(description = "Endret timestamp")
  val endretTimestamp: LocalDateTime? = null

)

fun StonadDto.toStonadEntity() = with(::Stonad) {
  val propertiesByName = StonadDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
    }
  })
}
