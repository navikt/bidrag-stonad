package no.nav.bidrag.stonad.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.persistence.entity.MottakerIdHistorikk
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class MottakerIdHistorikkDto(

  @ApiModelProperty(value = "stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Id til saksbehandler som oppretter stonaden")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for stonadet")
  val enhetId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun MottakerIdHistorikkDto.toMottakerIdHistorikkEntity() = with(::MottakerIdHistorikk) {
  val propertiesByName = MottakerIdHistorikkDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkEntity)
    }
  })
}
