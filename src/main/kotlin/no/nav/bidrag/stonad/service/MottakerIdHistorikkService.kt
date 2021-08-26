package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse
import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MottakerIdHistorikkService (val persistenceService: PersistenceService) {

  fun finnAlleEndringerMottakerIdForStonad(stonadId: Int): AlleMottakerIdHistorikkForStonadResponse {
    return AlleMottakerIdHistorikkForStonadResponse(persistenceService.finnAlleEndringerAvMottakerIdForStonad(stonadId))

  }
}
