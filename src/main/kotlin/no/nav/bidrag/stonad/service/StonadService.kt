package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadPeriodeDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.bo.OppdatertPeriode
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toPeriodeBo
import no.nav.bidrag.stonad.controller.StonadController
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.toPeriodeEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class StonadService(val persistenceService: PersistenceService) {

  private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)

  // Opprett komplett stønad (alle tabeller)
  fun opprettStonad(stonadRequest: OpprettStonadRequestDto): Int {
    val opprettetStonadId = persistenceService.opprettNyStonad(stonadRequest)
    // Perioder
    stonadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadId) }
    return opprettetStonadId
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettStonadPeriodeRequestDto, stonadId: Int) {
    persistenceService.opprettNyPeriode(periodeRequest.toPeriodeBo(), stonadId)
  }

  // Henter stønad ut fra stonadId
  fun hentStonadFraId(stonadId: Int): HentStonadDto? {
    val stonad = persistenceService.hentStonadFraId(stonadId)
    if (stonad != null) {
      val periodeListe = persistenceService.hentPerioderForStonad(stonadId)
      return lagHentStonadDto(stonad, periodeListe)
    } else return null
  }

  // Henter stønad ut fra unik nøkkel for stønad
  fun hentStonad(stonadType: String, skyldnerId: String, kravhaverId: String): HentStonadDto? {
    val stonad = persistenceService.hentStonad(stonadType, skyldnerId, kravhaverId)
    if (stonad != null) {
      val periodeListe = persistenceService.hentPerioderForStonad(stonad.stonadId)
      return lagHentStonadDto(stonad, periodeListe)
    } else return null
  }

  fun hentStonadInkludertUgyldiggjortePerioder(
    stonadType: String,
    skyldnerId: String,
    kravhaverId: String
  ): HentStonadDto? {
    val stonad = persistenceService.hentStonad(stonadType, skyldnerId, kravhaverId)
    if (stonad != null) {
      val periodeListe =
        persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(stonad.stonadId)
      return lagHentStonadDto(stonad, periodeListe)
    } else return null
  }

  fun lagHentStonadDto(stonad: Stonad, periodeListe: List<Periode>): HentStonadDto {
    val hentStonadPeriodeDtoListe = mutableListOf<HentStonadPeriodeDto>()
    periodeListe.forEach {
      hentStonadPeriodeDtoListe.add(
        HentStonadPeriodeDto(
          it.periodeId,
          it.periodeFom,
          it.periodeTil,
          stonad.stonadId,
          it.vedtakId,
          it.periodeGjortUgyldigAvVedtakId,
          it.belop,
          it.valutakode,
          it.resultatkode
        )
      )
    }

    return HentStonadDto(
      stonad.stonadId,
      StonadType.valueOf(stonad.stonadType),
      stonad.sakId,
      stonad.skyldnerId,
      stonad.kravhaverId,
      stonad.mottakerId,
      stonad.opprettetAv,
      stonad.opprettetTimestamp,
      stonad.endretAv,
      stonad.endretTimestamp,
      hentStonadPeriodeDtoListe
    )
  }

  fun endreStonad(eksisterendeStonad: HentStonadDto, oppdatertStonad: OpprettStonadRequestDto) {

    val stonadId = eksisterendeStonad.stonadId
    val endretAvSaksbehandlerId = oppdatertStonad.opprettetAv

    persistenceService.oppdaterStonad(stonadId, endretAvSaksbehandlerId)

    val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtakId

    eksisterendeStonad.periodeListe.forEach { periode ->
      val justertPeriode = finnOverlappPeriode(periode.toPeriodeBo(), oppdatertStonad)
      if (justertPeriode.settPeriodeSomUgyldig) {
        // Setter opprinnelige periode som ugyldig
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId)
      }
      // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
      if (justertPeriode.oppdaterPerioder) {
        justertPeriode.periodeListe.forEach {
          persistenceService.opprettNyPeriode(it, stonadId)
        }
      }
    }

    oppdatertStonad.periodeListe.forEach {
      persistenceService.opprettNyPeriode(it.toPeriodeBo(), stonadId)

    }
  }


  fun finnOverlappPeriode(eksisterendePeriode: PeriodeBo, oppdatertStonad: OpprettStonadRequestDto): OppdatertPeriode {
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

  fun endreMottakerIdOgOpprettHistorikk(request: EndreMottakerIdRequestDto): Int {
    persistenceService.endreMottakerId(request)
    return persistenceService.opprettNyMottakerIdHistorikk(request)
  }
}
