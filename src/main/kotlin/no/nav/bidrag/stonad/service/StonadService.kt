package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.FinnStonadResponse
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.api.NyStonadResponse
import no.nav.bidrag.stonad.api.toPeriodeDto
import no.nav.bidrag.stonad.controller.PeriodeController
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadService(val persistenceService: PersistenceService) {

  private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)

  // Opprett komplett stonad (alle tabeller)
  fun opprettStonad(stonadRequest: NyStonadRequest): NyStonadResponse {
    val stonadDto = StonadDto(
      stonadType = stonadRequest.stonadType,
      sakId = stonadRequest.sakId,
      skyldnerId = stonadRequest.skyldnerId,
      kravhaverId = stonadRequest.kravhaverId,
      mottakerId = stonadRequest.mottakerId,
      opprettetAvSaksbehandlerId = stonadRequest.opprettetAvSaksbehandlerId,
      endretAvSaksbehandlerId = stonadRequest.endretAvSaksbehandlerId
    )

    val opprettetStonad = persistenceService.opprettNyStonad(stonadDto)

    // Perioder
    stonadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonad.stonadId) }

    return NyStonadResponse(opprettetStonad.stonadId)
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: NyPeriodeRequest, stonadId: Int): PeriodeDto {
    return persistenceService.opprettNyPeriode(periodeRequest.toPeriodeDto(stonadId))

  }

  fun finnStonadFraId(stonadId: Int): FinnStonadResponse? {
    val stonadDto = persistenceService.finnStonadFraId(stonadId)
    if (stonadDto != null) {
      val periodeDtoListe = persistenceService.finnPerioderForStonad(stonadId)
      return lagFinnStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String): FinnStonadResponse? {
    val stonadDto = persistenceService.finnStonad(stonadType, skyldnerId, kravhaverId)
    if (stonadDto != null) {
      val periodeDtoListe = persistenceService.finnPerioderForStonad(stonadDto.stonadId)
      return lagFinnStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun finnStonadInkludertUgyldigePerioder(stonadType: String, skyldnerId: String, kravhaverId: String): FinnStonadResponse? {
    val stonadDto = persistenceService.finnStonad(stonadType, skyldnerId, kravhaverId)
    if (stonadDto != null) {
      val periodeDtoListe =
        persistenceService.finnPerioderForStonadInkludertUgyldige(stonadDto.stonadId)
      return lagFinnStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun lagFinnStonadResponse(stonadDto: StonadDto, periodeDtoListe: List<PeriodeDto>): FinnStonadResponse {
    return FinnStonadResponse(
      stonadDto.stonadId,
      stonadDto.stonadType,
      stonadDto.sakId,
      stonadDto.skyldnerId,
      stonadDto.kravhaverId,
      stonadDto.mottakerId,
      stonadDto.opprettetAvSaksbehandlerId,
      stonadDto.opprettetTimestamp,
      stonadDto.endretAvSaksbehandlerId,
      stonadDto.endretTimestamp,
      periodeDtoListe
    )
  }

  fun endreStonad(originalStonad: FinnStonadResponse, oppdatertStonad: NyStonadRequest) {
    val oppdatertStonadDto = StonadDto(
      stonadId = originalStonad.stonadId,
      stonadType = originalStonad.stonadType,
      sakId = originalStonad.sakId,
      skyldnerId = originalStonad.skyldnerId,
      kravhaverId = originalStonad.kravhaverId,
      mottakerId = originalStonad.mottakerId,
      opprettetAvSaksbehandlerId = originalStonad.opprettetAvSaksbehandlerId,
      endretAvSaksbehandlerId = oppdatertStonad.endretAvSaksbehandlerId
    )

    val oppdatertStonad = persistenceService.oppdaterStonad(oppdatertStonadDto)




  }

}























