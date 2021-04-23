package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.AlleStonadsendringerForstonadResponse
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.toStonadsendringDto
import no.nav.bidrag.stonad.dto.StonadsendringDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadMottakerIdHistorikkService (val persistenceService: PersistenceService) {

  fun opprettNyStonadsendring(request: NyStonadsendringRequest): StonadsendringDto {
    return persistenceService.opprettNyStonadsendring(request.toStonadsendringDto())
  }

  fun finnEnStonadsendring(stonadsendring_id: Int): StonadsendringDto {
    return persistenceService.finnEnStonadsendring(stonadsendring_id)
  }

  fun finnAlleStonadsendringerForstonad(stonadId: Int): no.nav.bidrag.stonad.api.AlleStonadsendringerForstonadResponse {
    return no.nav.bidrag.stonad.api.AlleStonadsendringerForstonadResponse(
      persistenceService.finnAlleStonadsendringerForstonad(
        stonadId
      )
    )
  }
}
