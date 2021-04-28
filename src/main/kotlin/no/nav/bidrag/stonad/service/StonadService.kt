package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.api.NyStonadResponse
import no.nav.bidrag.stonad.api.FinnStonadResponse
import no.nav.bidrag.stonad.api.toPeriodeDto
import no.nav.bidrag.stonad.controller.PeriodeController
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadService (val persistenceService: PersistenceService) {

  private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)

  // Opprett komplett stonad (alle tabeller)
  fun opprettStonad(stonadRequest: NyStonadRequest): NyStonadResponse {
    val stonadDto = StonadDto(stonadType = stonadRequest.stonadType, sakId = stonadRequest.sakId,
      behandlingId = stonadRequest.behandlingId, skyldnerId = stonadRequest.skyldnerId,
      kravhaverId = stonadRequest.kravhaverId, mottakerId = stonadRequest.mottakerId)

    val opprettetStonad = persistenceService.opprettNyStonad(stonadDto)

    // Perioder
    stonadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonad.stonadId) }

    return NyStonadResponse(opprettetStonad.stonadId)
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: NyPeriodeRequest, stonadId: Int): PeriodeDto {
    return persistenceService.opprettNyPeriode(periodeRequest.toPeriodeDto(stonadId))

  }

  fun finnStonad(stonadId: Int): FinnStonadResponse {
    val stonadDto = persistenceService.finnStonad(stonadId)
    val periodeDtoListe = persistenceService.finnAllePerioderForStonad(stonadId)
/*    val periodeListe = ArrayList<PeriodeResponse>()
    periodeDtoListe.forEach {
      periodeListe.add(PeriodeResponse(it.periodeFomDato, it.periodeTilDato, it.belop, it.valutakode, it.resultatkode))
    }*/
    return FinnStonadResponse(stonadDto.stonadType, stonadDto.sakId, stonadDto.behandlingId,
      stonadDto.skyldnerId, stonadDto.kravhaverId, stonadDto.mottakerId, stonadDto.opprettetAvSaksbehandlerId,
      stonadDto.opprettetTimestamp, stonadDto.endretAvSaksbehandlerId, stonadDto.endretTimestamp, periodeDtoListe)
    }

}
