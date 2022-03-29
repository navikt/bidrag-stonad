package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MottakerIdHistorikkService (val persistenceService: PersistenceService) {

  fun hentAlleEndringerAvMottakerIdForStonad(stonadId: Int): AlleMottakerIdHistorikkForStonadResponse {
    return AlleMottakerIdHistorikkForStonadResponse(persistenceService.hentAlleEndringerAvMottakerIdForStonad(stonadId))

  }
}
