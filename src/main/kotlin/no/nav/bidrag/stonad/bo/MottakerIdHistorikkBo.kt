package no.nav.bidrag.stonad.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.persistence.entity.MottakerIdHistorikk
import no.nav.bidrag.stonad.persistence.entity.Stonad
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class MottakerIdHistorikkBo(

  @Schema(description = "stonad-id")
  val stonadId: Int = 0,

  @Schema(description = "Utgått Mottaker-Id")
  val mottakerIdEndretFra: String = "",

  @Schema(description = "Ny Mottaker-Id")
  val mottakerIdEndretTil: String = "",

  @Schema(description = "Saksbehandler som har oppdatert mottaker-id")
  val opprettetAv: String = "",

  @Schema(description = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun MottakerIdHistorikkBo.toMottakerIdHistorikkEntity(stonad: Stonad) = with(::MottakerIdHistorikk) {
  val propertiesByName = MottakerIdHistorikkBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      MottakerIdHistorikk::stonad.name -> stonad
      else -> propertiesByName[parameter.name]?.get(this@toMottakerIdHistorikkEntity)
    }
  })
}
