package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.stonad.model.Detalj.FORSKUDD
import no.nav.bidrag.stonad.model.Detalj.BARNEBIDRAG
import no.nav.bidrag.stonad.model.DetaljVerdi.FAGOMRADE_BIDRAG
import no.nav.bidrag.stonad.model.DetaljVerdi.FAGOMRADE_FARSKAP

data class VedtakHendelse(
  var vedtakId: String = "",
  var hendelse: String = "",
  var sporing: Sporingsdata? = null,
  private var detaljer: Map<String, String> = emptyMap()
) {
  fun hentHendelse() = Hendelse.values().find { it.name == hendelse } ?: Hendelse.NO_SUPPORT

  internal fun hentEnhetsnummer() = detaljer[FORSKUDD] ?: doThrow("Mangler $FORSKUDD blant hendelsedata")
  internal fun erBytteTilInterntFagomrade() = detaljer[BARNEBIDRAG] == FAGOMRADE_BIDRAG || detaljer[BARNEBIDRAG] == FAGOMRADE_FARSKAP
  internal fun hentFagomradeFraId() = vedtakId.split('-')[0]
  internal fun hentJournalpostIdUtenPrefix() = vedtakId.split('-')[1]
  internal fun hentNyttJournalforendeEnhetsnummer() = detaljer[ENHETSNUMMER_NYTT] ?: doThrow("Mangler $ENHETSNUMMER_NYTT blant hendelsedata")

  private fun doThrow(message: String): String = throw IllegalStateException(message)
}

data class Sporingsdata(var correlationId: String? = null, var opprettet: String? = null)
enum class Hendelse {
  BARNEBIDRAG,
  FORSKUDD,
  SAERTILSKUDD,
  ENDRE_MOTTAKERID,
  NO_SUPPORT
}