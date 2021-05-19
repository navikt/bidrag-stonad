package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.stonad.dto.PeriodeDto

import java.time.LocalDateTime

data class VedtakHendelse(
  var stonadId: Int = 0,
  var stonadType: String = "",
  var sakId: String? = null,
  var skyldnerId: String = "",
  var kravhaverId: String = "",
  var mottakerId: String = "",
  var opprettetAvSaksbehandlerId: String = "",
  var opprettetTimestamp: LocalDateTime = LocalDateTime.now(),
  var endretAvSaksbehandlerId: String = "",
  var endretTimestamp: LocalDateTime = LocalDateTime.now(),
  var periodeListe: List<PeriodeDto>,

  var sporing: Sporingsdata? = null)
{
  fun hentStonadType() = StonadType.values().find { it.name == stonadType } ?: StonadType.NO_SUPPORT

  private fun doThrow(message: String): String = throw IllegalStateException(message)
}

data class Sporingsdata(var correlationId: String? = null, var opprettet: String? = null)

enum class StonadType {
  BARNEBIDRAG,
  FORSKUDD,
  SAERTILSKUDD,
  ENDRE_MOTTAKERID,
  NO_SUPPORT
}