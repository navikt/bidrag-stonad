package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.model.VedtakHendelsePeriode

import java.time.LocalDateTime

data class VedtakHendelse(
  var vedtakId: Int,
  val vedtakType: VedtakType,
  var stonadType: StonadType,
  var sakId: String? = null,
  var skyldnerId: String,
  var kravhaverId: String,
  var mottakerId: String,
  var opprettetAv: String,
  var opprettetTimestamp: LocalDateTime,
  var periodeListe: List<VedtakHendelsePeriode>,

  var sporing: Sporingsdata? = null)
{
  fun hentStonadType() = StonadType.values().find { it.name == stonadType.toString() } ?: StonadType.NO_SUPPORT

  private fun doThrow(message: String): String = throw IllegalStateException(message)
}

data class Sporingsdata(var correlationId: String? = null, var opprettet: String? = null)

