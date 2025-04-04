package no.nav.bidrag.stonad.service

import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService(val persistenceService: PersistenceService) {
    fun hentPeriode(periodeId: Int): StønadPeriodeDto? = persistenceService.hentPeriode(periodeId)
}
