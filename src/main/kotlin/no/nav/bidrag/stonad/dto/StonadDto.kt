package no.nav.bidrag.stonad.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.MottakerIdHistorikk
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class StonadDto(

  @ApiModelProperty(value = "Stønadsendring-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty("Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "Id til saksbehandler som oppretter stønaden")
  val opprettetAvSaksbehandlerId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Id til saksbehandler som sist endret stønaden")
  val endretAvSaksbehandlerId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val endretTimestamp: LocalDateTime = LocalDateTime.now()

)

fun StonadDto.toStonadEntity() = with(::Stonad) {
  val propertiesByName = StonadDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
    }
  })
}
