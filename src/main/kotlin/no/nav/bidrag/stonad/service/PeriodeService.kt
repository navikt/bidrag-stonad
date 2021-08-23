package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.toPeriodeDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService (val persistenceService: PersistenceService) {

  fun finnPeriode(periodeId: Int): PeriodeDto? {
    return persistenceService.finnPeriode(periodeId)
  }
}