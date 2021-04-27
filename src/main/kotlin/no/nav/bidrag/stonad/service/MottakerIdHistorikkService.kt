package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.NyEndreMottakerIdRequest
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MottakerIdHistorikkService (val persistenceService: PersistenceService) {

/*  fun endreMottakerId(request: NyEndreMottakerIdRequest): MottakerIdHistorikkDto {
    return persistenceService.endreMottakerId(request.toMottakerIdDto())
  }*/

  fun finnAlleEndringerAvMottakerIdForStonad(stonadId: Int): List<MottakerIdHistorikkDto> {
    return persistenceService.finnAlleEndringerAvMottakerIdForStonad(stonadId)

  }
}
