package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.AllePerioderForStonadsendringResponse
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.toPeriodeDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService (val persistenceService: PersistenceService) {

  fun opprettNyPeriode(request: NyPeriodeRequest): PeriodeDto {
    return persistenceService.opprettNyPeriode(request.toPeriodeDto())
  }

  fun finnPeriode(periodeId: Int): PeriodeDto {
    return persistenceService.finnPeriode(periodeId)
  }

  fun finnAllePerioderForStonadsendring(stonadsendringId: Int): no.nav.bidrag.stonad.api.AllePerioderForStonadsendringResponse {
    return no.nav.bidrag.stonad.api.AllePerioderForStonadsendringResponse(
      persistenceService.finnAllePerioderForStonadsendring(
        stonadsendringId
      )
    )
  }

}
