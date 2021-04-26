package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.api.toStonadDto
import no.nav.bidrag.stonad.dto.StonadDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MottakerIdHistorikkService (val persistenceService: PersistenceService) {

  fun endreMottakerId(request: EndreMottakerIdRequest): EndreMottakerIdResponse {
    return persistenceService.endreMottakerId(request.toMottakerIdDto())
  }

  fun finnAlleEndringerAvMottakerIdForStonad(stonadId: Int): List<MottakerIdDto> {
    return persistenceService.finnAlleEndringerAvMottakerIdForStonad(stonadId)
    )
  }
}
