package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.bo.PeriodeBo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService (val persistenceService: PersistenceService) {

  fun finnPeriode(periodeId: Int): PeriodeBo? {
    return persistenceService.finnPeriode(periodeId)
  }
}