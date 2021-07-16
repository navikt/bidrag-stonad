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

  fun finnStonadInkludertUgyldigePerioder(
    stonadType: String,
    skyldnerId: String,
    kravhaverId: String
  ): FinnStonadResponse? {
    val stonadDto = persistenceService.finnStonad(stonadType, skyldnerId, kravhaverId)
    if (stonadDto != null) {
      val periodeDtoListe =
        persistenceService.finnPerioderForStonadInkludertUgyldige(stonadDto.stonadId)
      return lagFinnStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun lagFinnStonadResponse(
    stonadDto: StonadDto,
    periodeDtoListe: List<PeriodeDto>
  ): FinnStonadResponse {
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

    val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtakId

    eksisterendeStonad.periodeListe.forEach { periode ->
      val justertPeriode = finnOverlappPeriode(periode, oppdatertStonad)
      if (justertPeriode.settPeriodeSomUgyldig) {
        // Setter opprinnelige periode som ugyldig og lager en ny periode med like data, men med ny fom-dato
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
        // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
        if (justertPeriode.oppdaterPeriode) {
          persistenceService.opprettNyPeriode(justertPeriode.periode)
        }
      }
    }

    oppdatertStonad.periodeListe.forEach {
      persistenceService.opprettNyPeriode(it.toPeriodeDto(stonadId))
    }
  }


  fun finnOverlappPeriode(periode: PeriodeDto, oppdatertStonad: NyStonadRequest): OppdatertPeriode {
    val oppdatertStonadDatoFom = oppdatertStonad.periodeListe.first().periodeFom
    val oppdatertStonadDatoTil = oppdatertStonad.periodeListe.last().periodeTil
    if (periode.periodeFom.isBefore(oppdatertStonadDatoFom)) {
      if (periode.periodeTil == null || periode.periodeTil.isAfter(oppdatertStonadDatoFom)) {
        return OppdatertPeriode(lagNyPeriodeMedEndretTilDato(periode, oppdatertStonadDatoFom), true, false)
      } else {
        return OppdatertPeriode(periode, false, false)
      }
    } else if (oppdatertStonadDatoTil == null) {
      return OppdatertPeriode(periode, false, false)
    } else if (periode.periodeFom.isAfter(oppdatertStonadDatoTil.minusDays(1))) {
      return OppdatertPeriode(periode, false, false)
    } else if (periode.periodeTil == null) {
      return OppdatertPeriode(lagNyPeriodeMedEndretFomDato(periode, oppdatertStonadDatoFom), true, false)
    } else if (periode.periodeTil.isBefore(oppdatertStonadDatoTil.plusDays(1))) {
      return OppdatertPeriode(lagNyPeriodeMedEndretFomDato(periode, oppdatertStonadDatoFom), true, false)
    } else return OppdatertPeriode(lagNyPeriodeMedEndretFomDato(periode, oppdatertStonadDatoFom),true,false
    )
  }

  fun lagNyPeriodeMedEndretFomDato(periode: PeriodeDto, nyFomDato: LocalDate): PeriodeDto {
//    persistenceService.opprettNyPeriode(
    return PeriodeDto(
      periodeFom = nyFomDato,
      periodeTil = periode.periodeTil,
      stonadId = periode.stonadId,
      vedtakId = periode.vedtakId,
      periodeGjortUgyldigAvVedtakId = null,
      belop = periode.belop,
      valutakode = periode.valutakode,
      resultatkode = periode.resultatkode
    )
  }

  fun lagNyPeriodeMedEndretTilDato(periode: PeriodeDto, nyTilDato: LocalDate): PeriodeDto {
//    persistenceService.opprettNyPeriode(
    return PeriodeDto(
      periodeFom = periode.periodeFom,
      periodeTil = nyTilDato,
      stonadId = periode.stonadId,
      vedtakId = periode.vedtakId,
      periodeGjortUgyldigAvVedtakId = null,
      belop = periode.belop,
      valutakode = periode.valutakode,
      resultatkode = periode.resultatkode
    )
  }
}

data class OppdatertPeriode(
  val periode: PeriodeDto,
  val oppdaterPeriode: Boolean = false,
  val settPeriodeSomUgyldig: Boolean = false
)
