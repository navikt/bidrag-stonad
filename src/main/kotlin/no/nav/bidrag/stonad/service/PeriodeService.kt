package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService(val persistenceService: PersistenceService) {

    fun hentPeriode(periodeId: Int): StonadPeriodeDto? {
        return persistenceService.hentPeriode(periodeId)
    }
}
