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
import java.time.LocalDate

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

  fun endreStonad(eksisterendeStonad: FinnStonadResponse, oppdatertStonad: NyStonadRequest) {

    val stonadId = eksisterendeStonad.stonadId
    val endretAvSaksbehandlerId = oppdatertStonad.endretAvSaksbehandlerId

    persistenceService.oppdaterStonad(stonadId, endretAvSaksbehandlerId)

    val oppdatertStonadDatoFom = oppdatertStonad.periodeListe.first().periodeFom
    val oppdatertStonadDatoTil = oppdatertStonad.periodeListe.last().periodeTil
    val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtakId

    eksisterendeStonad.periodeListe.forEach { periode ->
      val statusOverlapp = finnOverlappPeriode(periode, oppdatertStonadDatoFom, oppdatertStonadDatoTil)
      if (statusOverlapp == "OverlappEndreFomDato") {
        // Setter opprinnelige periode som ugyldig og lager en ny periode med like data, men med ny fom-dato
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
        lagNyPeriodeMedEndretFomDato(periode, oppdatertStonadDatoTil!!)
      } else if ( statusOverlapp == "OverlappEndreTildato") {
        // Setter opprinnelige periode som ugyldig og lager en ny periode med like data, men med ny til-dato
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
        lagNyPeriodeMedEndretTilDato(periode, oppdatertStonadDatoFom)
      } else if ( statusOverlapp == "FullOverLapp") {
        // Setter opprinnelige periode som ugyldig
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
      }
    }
    oppdatertStonad.periodeListe.forEach {
      persistenceService.opprettNyPeriode(it.toPeriodeDto(stonadId))
    }

  }

  fun finnOverlappPeriode(periode: PeriodeDto, endringerStonadDatoFom: LocalDate, endringerStonadDatoTil: LocalDate?): String {
    var statusOverlapp: String = ""
    if (periode.periodeFom.isBefore(endringerStonadDatoFom)) {
      statusOverlapp = if (periode.periodeTil == null || periode.periodeTil.isAfter(endringerStonadDatoFom)) {
        "OverlappEndreTildato"
      } else {
        "IngenOverlapp"
      }
    } else if (endringerStonadDatoTil == null) {
      statusOverlapp = "FullOverLapp"
    } else if (periode.periodeFom.isAfter(endringerStonadDatoTil.minusDays(1))) {
      statusOverlapp = "IngenOverlapp"
      } else if (periode.periodeTil == null) {
        statusOverlapp = "OverlappEndreFomDato"
      } else if (periode.periodeTil.isBefore(endringerStonadDatoTil.plusDays(1))) {
      statusOverlapp = "FullOverlapp"
    } else statusOverlapp = "OverlappEndreFomDato"
    return statusOverlapp
  }

  fun lagNyPeriodeMedEndretFomDato(periode: PeriodeDto, nyFomDato: LocalDate) {
    persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = nyFomDato,
        periodeTil = periode.periodeTil,
        stonadId = periode.stonadId,
        vedtakId = periode.vedtakId,
        periodeGjortUgyldigAvVedtakId = null,
        belop = periode.belop,
        valutakode = periode.valutakode,
        resultatkode = periode.resultatkode
      )
    )
  }

  fun lagNyPeriodeMedEndretTilDato(periode: PeriodeDto, nyTilDato: LocalDate) {
    persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = periode.periodeFom,
        periodeTil = nyTilDato,
        stonadId = periode.stonadId,
        vedtakId = periode.vedtakId,
        periodeGjortUgyldigAvVedtakId = null,
        belop = periode.belop,
        valutakode = periode.valutakode,
        resultatkode = periode.resultatkode
      )
    )
  }

}























