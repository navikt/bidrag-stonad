package no.nav.bidrag.stonad.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.persistence.entity.MottakerIdHistorikk
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class MottakerIdHistorikkDto(

  @ApiModelProperty(value = "stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Utgått Mottaker-Id")
  val mottakerIdEndretFra: String = "",

  @ApiModelProperty(value = "Ny Mottaker-Id")
  val mottakerIdEndretTil: String = "",

  @ApiModelProperty(value = "Saksbehandler som har oppdatert mottaker-id")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun MottakerIdHistorikkDto.toMottakerIdHistorikkEntity(stonad: Stonad) = with(::MottakerIdHistorikk) {
  val propertiesByName = MottakerIdHistorikkDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      MottakerIdHistorikk::stonad.name -> stonad
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkEntity)
    }
  })
}
