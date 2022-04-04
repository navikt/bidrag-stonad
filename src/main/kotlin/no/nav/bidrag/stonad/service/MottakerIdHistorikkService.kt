package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.AlleMottakerIdHistorikkForStonadDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MottakerIdHistorikkService (val persistenceService: PersistenceService) {

  fun hentAlleEndringerAvMottakerIdForStonad(stonadId: Int): AlleMottakerIdHistorikkForStonadDto {
    return AlleMottakerIdHistorikkForStonadDto(persistenceService.hentAlleEndringerAvMottakerIdForStonad(stonadId))

  }
}
