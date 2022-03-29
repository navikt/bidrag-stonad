package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.api.HentStonadResponse
import no.nav.bidrag.stonad.api.OpprettPeriodeRequest
import no.nav.bidrag.stonad.api.OpprettStonadRequest
import no.nav.bidrag.stonad.api.OpprettStonadResponse
import no.nav.bidrag.stonad.api.toPeriodeBo
import no.nav.bidrag.stonad.bo.MottakerIdHistorikkBo
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.StonadBo
import no.nav.bidrag.stonad.controller.StonadController
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class StonadService(val persistenceService: PersistenceService) {

  private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)

  // Opprett komplett stonad (alle tabeller)
  fun opprettStonad(stonadRequest: OpprettStonadRequest): Int {
    val stonadBo = StonadBo(
      stonadType = stonadRequest.stonadType.toString(),
      sakId = stonadRequest.sakId,
      skyldnerId = stonadRequest.skyldnerId,
      kravhaverId = stonadRequest.kravhaverId,
      mottakerId = stonadRequest.mottakerId,
      opprettetAv = stonadRequest.opprettetAv
    )

    val opprettetStonad = persistenceService.opprettNyStonad(stonadBo)

    // Perioder
    stonadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonad.stonadId) }

    return opprettetStonad.stonadId
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettPeriodeRequest, stonadId: Int): PeriodeBo {
    return persistenceService.opprettNyPeriode(periodeRequest.toPeriodeBo(stonadId))

  }

  fun hentStonadFraId(stonadId: Int): HentStonadResponse? {
    val stonadDto = persistenceService.hentStonadFraId(stonadId)
    if (stonadDto != null) {
      val periodeDtoListe = persistenceService.hentPerioderForStonad(stonadId)
      return lagHentStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun hentStonad(stonadType: String, skyldnerId: String, kravhaverId: String): HentStonadResponse? {
    val stonadDto = persistenceService.finnStonad(stonadType, skyldnerId, kravhaverId)
    if (stonadDto != null) {
      val periodeDtoListe = persistenceService.hentPerioderForStonad(stonadDto.stonadId)
      return lagHentStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun hentStonadInkludertUgyldiggjortePerioder(
    stonadType: String,
    skyldnerId: String,
    kravhaverId: String
  ): HentStonadResponse? {
    val stonadDto = persistenceService.finnStonad(stonadType, skyldnerId, kravhaverId)
    if (stonadDto != null) {
      val periodeDtoListe =
        persistenceService.finnPerioderForStonadInkludertUgyldiggjorte(stonadDto.stonadId)
      return lagHentStonadResponse(stonadDto, periodeDtoListe)
    } else return null
  }

  fun lagHentStonadResponse(
    stonadBo: StonadBo,
    periodeBoListe: List<PeriodeBo>
  ): HentStonadResponse {
    return HentStonadResponse(
      stonadBo.stonadId,
      StonadType.valueOf(stonadBo.stonadType),
      stonadBo.sakId,
      stonadBo.skyldnerId,
      stonadBo.kravhaverId,
      stonadBo.mottakerId,
      stonadBo.opprettetAv,
      stonadBo.opprettetTimestamp,
      stonadBo.endretAv,
      stonadBo.endretTimestamp,
      periodeBoListe
    )
  }

  fun endreStonad(eksisterendeStonad: HentStonadResponse, oppdatertStonad: OpprettStonadRequest) {

    val stonadId = eksisterendeStonad.stonadId
    val endretAvSaksbehandlerId = oppdatertStonad.endretAv

    persistenceService.oppdaterStonad(stonadId, endretAvSaksbehandlerId!!)

    val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtakId

    eksisterendeStonad.periodeListe.forEach { periode ->
      val justertPeriode = finnOverlappPeriode(periode, oppdatertStonad)
      if (justertPeriode.settPeriodeSomUgyldig) {
        // Setter opprinnelige periode som ugyldig
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
      }
      // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
      if (justertPeriode.oppdaterPerioder) {
        justertPeriode.periodeListe.forEach {
          persistenceService.opprettNyPeriode(it)
        }
      }
    }

    oppdatertStonad.periodeListe.forEach {
      persistenceService.opprettNyPeriode(it.toPeriodeBo(stonadId))

    }
  }


  fun finnOverlappPeriode(eksisterendePeriode: PeriodeBo, oppdatertStonad: OpprettStonadRequest): OppdatertPeriode {
    val periodeBoListe = mutableListOf<PeriodeBo>()
    val oppdatertStonadDatoFom = oppdatertStonad.periodeListe.first().periodeFom
    val oppdatertStonadDatoTil = oppdatertStonad.periodeListe.last().periodeTil
    if (eksisterendePeriode.periodeFom.isBefore(oppdatertStonadDatoFom)) {
      if (eksisterendePeriode.periodeTil == null || eksisterendePeriode.periodeTil.isAfter(oppdatertStonadDatoFom)){
        // Perioden overlapper. Eksisterende periode må settes som ugyldig og ny periode opprettes med korrigert til-dato.
        periodeBoListe.add(lagNyPeriodeMedEndretTilDato(eksisterendePeriode, oppdatertStonadDatoFom))
        if (oppdatertStonadDatoTil != null && (eksisterendePeriode.periodeTil == null || eksisterendePeriode.periodeTil.isAfter(oppdatertStonadDatoTil))){
          periodeBoListe.add(lagNyPeriodeMedEndretFomDato(eksisterendePeriode, oppdatertStonadDatoTil))
        }
        return OppdatertPeriode(periodeBoListe,true,true)
      }

    } else if (oppdatertStonadDatoTil == null) {
      periodeBoListe.add(eksisterendePeriode)
      return OppdatertPeriode(periodeBoListe, false, true)

    } else if (eksisterendePeriode.periodeFom.isAfter(oppdatertStonadDatoTil.minusDays(1))) {
      periodeBoListe.add(eksisterendePeriode)
      return OppdatertPeriode(periodeBoListe, false, false)
    } else if (eksisterendePeriode.periodeTil == null || eksisterendePeriode.periodeTil.isAfter(oppdatertStonadDatoTil)) {
      periodeBoListe.add(lagNyPeriodeMedEndretFomDato(eksisterendePeriode, oppdatertStonadDatoTil))
      return OppdatertPeriode(periodeBoListe, true,true)

    } else if (eksisterendePeriode.periodeTil.isBefore(oppdatertStonadDatoTil.plusDays(1))) {
      periodeBoListe.add(eksisterendePeriode)
      return OppdatertPeriode(periodeBoListe,false,true)
    }
    else
      periodeBoListe.add(eksisterendePeriode)
      return OppdatertPeriode(periodeBoListe, false, false)
  }

  fun lagNyPeriodeMedEndretFomDato(periode: PeriodeBo, nyFomDato: LocalDate): PeriodeBo {
//    persistenceService.opprettNyPeriode(
    return PeriodeBo(
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

  fun lagNyPeriodeMedEndretTilDato(periode: PeriodeBo, nyTilDato: LocalDate): PeriodeBo {
//    persistenceService.opprettNyPeriode(
    return PeriodeBo(
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

  fun endreMottakerIdOgOpprettHistorikk(request: EndreMottakerIdRequest): MottakerIdHistorikkBo {
    persistenceService.endreMottakerId(request.stonadId, request.nyMottakerId, request.opprettetAv)

    return persistenceService.opprettNyMottakerIdHistorikk(request)
  }
}

data class OppdatertPeriode(
  val periodeListe: List<PeriodeBo>,
  val oppdaterPerioder: Boolean = false,
  val settPeriodeSomUgyldig: Boolean = false
)
