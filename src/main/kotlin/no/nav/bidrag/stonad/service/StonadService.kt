package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadHistoriskRequest
import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.bo.OppdatertPeriode
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toPeriodeBo
import no.nav.bidrag.stonad.controller.StonadController
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class StonadService(val persistenceService: PersistenceService) {

  private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)

  // Disse metodene brukes av endepunktet for å opprette en stønad. Bør vel fjernes siden alt kommer via Kafka
  // Opprett komplett stønad (alle tabeller)
  fun opprettStonad(stonadRequest: OpprettStonadRequestDto): Int {
    val opprettetStonadId = persistenceService.opprettStonad(stonadRequest)
    // Perioder
    stonadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadId) }
    return opprettetStonadId
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettStonadPeriodeRequestDto, stonadId: Int) {
    persistenceService.opprettPeriode(periodeRequest.toPeriodeBo(), stonadId)
  }

  // Henter stønad ut fra stonadId
  fun hentStonadFraId(stonadId: Int): StonadDto? {
    val stonad = persistenceService.hentStonadFraId(stonadId)
    if (stonad != null) {
      val periodeListe = persistenceService.hentPerioderForStonad(stonadId)
      return lagStonadDto(stonad, periodeListe)
    } else return null
  }

  // Henter stønad ut fra unik nøkkel for stønad
  fun hentStonad(request: HentStonadRequest): StonadDto? {
    val stonad = persistenceService.hentStonad(request.type.toString(), request.skyldnerId, request.kravhaverId, request.sakId)
    if (stonad != null) {
      val periodeListe = persistenceService.hentPerioderForStonad(stonad.stonadId)
      return lagStonadDto(stonad, periodeListe)
    } else return null
  }

  fun hentStonadInkludertUgyldiggjortePerioder(
    stonadType: String,
    skyldnerId: String,
    kravhaverId: String,
    sakId: String
  ): StonadDto? {
    val stonad = persistenceService.hentStonad(stonadType, skyldnerId, kravhaverId, sakId)
    if (stonad != null) {
      val periodeListe =
        persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(stonad.stonadId)
      return lagStonadDto(stonad, periodeListe)
    } else return null
  }

  fun hentStonadHistorisk(request: HentStonadHistoriskRequest): StonadDto? {
    val stonad = persistenceService.hentStonad(request.type.toString(), request.skyldnerId, request.kravhaverId, request.sakId)
    if (stonad != null) {
      val periodeListe =
        persistenceService.hentPerioderForStonadForAngittTidspunkt(stonad.stonadId, request.gyldigTidspunkt)
      return lagStonadDto(stonad, periodeListe)
    } else return null
  }

  fun lagStonadDto(stonad: Stonad, periodeListe: List<Periode>): StonadDto {
    val hentStonadPeriodeDtoListe = mutableListOf<StonadPeriodeDto>()
    periodeListe.forEach {
      hentStonadPeriodeDtoListe.add(
        StonadPeriodeDto(
          it.periodeId,
          it.periodeFom,
          it.periodeTil,
          stonad.stonadId,
          it.vedtakId,
          it.gyldigFra,
          it.gyldigTil,
          it.periodeGjortUgyldigAvVedtakId,
          it.belop,
          it.valutakode,
          it.resultatkode
        )
      )
    }

    return StonadDto(
      stonad.stonadId,
      StonadType.valueOf(stonad.type),
      stonad.sakId,
      stonad.skyldnerId,
      stonad.kravhaverId,
      stonad.mottakerId,
      stonad.indeksreguleringAar,
      Innkreving.valueOf(stonad.innkreving),
      stonad.opprettetAv,
      stonad.opprettetTidspunkt,
      stonad.endretAv,
      stonad.endretTidspunkt,
      hentStonadPeriodeDtoListe
    )
  }

  fun endreStonad(eksisterendeStonad: StonadDto, oppdatertStonad: OpprettStonadRequestDto, vedtakTidspunkt: LocalDateTime) {

    val stonadId = eksisterendeStonad.stonadId
    val endretAvSaksbehandlerId = oppdatertStonad.opprettetAv

    persistenceService.oppdaterStonad(stonadId, endretAvSaksbehandlerId)

    val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtakId

    eksisterendeStonad.periodeListe.forEach { periode ->
      val justertPeriode = finnOverlappPeriode(periode.toPeriodeBo(), oppdatertStonad)
      if (justertPeriode.settPeriodeSomUgyldig) {
        // Setter opprinnelige periode som ugyldig
        persistenceService.settPeriodeSomUgyldig(periode.periodeId, oppdatertStonadVedtakId, vedtakTidspunkt)
      }
      // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
      if (justertPeriode.oppdaterPerioder) {
        justertPeriode.periodeListe.forEach {
          persistenceService.opprettJustertPeriode(it, stonadId, vedtakTidspunkt)
        }
      }
    }

    oppdatertStonad.periodeListe.forEach {
      // Sjekk om beløp for ny periode = null, det er da et opphørsvedtak og periode skal ikke lagres.
      // Sjekken må gjøres etter at de eksisterende periodene er behandlet
      if (it.belop != null) {
        persistenceService.opprettPeriode(it.toPeriodeBo(), stonadId)
      }
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
}
