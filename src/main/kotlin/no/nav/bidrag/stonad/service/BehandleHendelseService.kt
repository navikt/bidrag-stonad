package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

private val LOGGER = LoggerFactory.getLogger(DefaultBehandleHendelseService::class.java)

interface BehandleHendelseService {
  fun behandleHendelse(vedtakHendelse: VedtakHendelse)
}

@Service
class DefaultBehandleHendelseService(
  private val stonadService: StonadService,
  private val persistenceService: PersistenceService
) : BehandleHendelseService {

  override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
    LOGGER.info("Behandler vedtakHendelse for vedtakid: ${vedtakHendelse.id}")
    SECURE_LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

    vedtakHendelse.stonadsendringListe?.forEach() { stonadsendring ->
      behandleVedtakHendelse(stonadsendring, vedtakHendelse.type, vedtakHendelse.id, vedtakHendelse.opprettetAv, vedtakHendelse.vedtakTidspunkt)
    }
  }

  private fun behandleVedtakHendelse(
      stonadsendring: Stonadsendring, vedtakType: VedtakType, vedtakId: Int, opprettetAv: String, vedtakTidspunkt: LocalDateTime) {
//    Sjekker om stonad skal oppdateres
    if (stonadsendring.endring) {
      val eksisterendeStonad = stonadService.hentStonad(
        HentStonadRequest(stonadsendring.type, stonadsendring.sakId, stonadsendring.skyldnerId, stonadsendring.kravhaverId)
      )

      if (eksisterendeStonad != null) {
        if (vedtakType == VedtakType.ENDRING_MOTTAKER) {
          // Mottatt hendelse skal oppdatere mottakerId for alle stønader i stonadsendringListe. Ingen perioder skal oppdateres.
          persistenceService.endreMottakerId(eksisterendeStonad.stonadId, stonadsendring.mottakerId, opprettetAv)
        } else {
          // Mottatt Hendelse skal oppdatere eksisterende stønad
          endreStonad(eksisterendeStonad, stonadsendring, vedtakId, opprettetAv, vedtakTidspunkt)
        }
      } else {
        opprettStonad(stonadsendring, vedtakId, opprettetAv, vedtakTidspunkt)
      }
    }
  }

  private fun endreStonad(
      eksisterendeStonad: StonadDto, stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String, vedtakTidspunkt: LocalDateTime
  ) {
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    val hendelsePeriodeListe = stonadsendring.periodeListe.sortedBy { it.fomDato }
    var i = 1
    hendelsePeriodeListe.forEach {
      periodeListe.add(
        OpprettStonadPeriodeRequestDto(
          periodeFom = it.fomDato,
          periodeTil = finnPeriodeTil(it.tilDato, hendelsePeriodeListe, i),
          vedtakId = vedtakId,
          gyldigFra = vedtakTidspunkt,
          gyldigTil = null,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
      i++
    }

    val oppdatertStonad =
      OpprettStonadRequestDto(
        type = stonadsendring.type,
        sakId = stonadsendring.sakId,
        skyldnerId = stonadsendring.skyldnerId,
        kravhaverId = stonadsendring.kravhaverId,
        mottakerId = stonadsendring.mottakerId,
        indeksreguleringAar = stonadsendring.indeksreguleringAar,
        innkreving = stonadsendring.innkreving,
        opprettetAv = opprettetAv,
        periodeListe = periodeListe
      )

    stonadService.endreStonad(eksisterendeStonad, oppdatertStonad, vedtakTidspunkt)

  }

  private fun opprettStonad(stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String, vedtakTidspunkt: LocalDateTime) {
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    val hendelsePeriodeListe = stonadsendring.periodeListe.sortedBy { it.fomDato }
    var i = 1
    hendelsePeriodeListe.forEach {
      periodeListe.add(
        OpprettStonadPeriodeRequestDto(
          periodeFom = it.fomDato,
          periodeTil = finnPeriodeTil(it.tilDato, hendelsePeriodeListe, i),
          vedtakId = vedtakId,
          gyldigFra = vedtakTidspunkt,
          gyldigTil = null,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
      i++
    }

    stonadService.opprettStonad(
      OpprettStonadRequestDto(
        type = stonadsendring.type,
        sakId = stonadsendring.sakId,
        skyldnerId = stonadsendring.skyldnerId,
        kravhaverId = stonadsendring.kravhaverId,
        mottakerId = stonadsendring.mottakerId,
        indeksreguleringAar = stonadsendring.indeksreguleringAar,
        innkreving = stonadsendring.innkreving,
        opprettetAv = opprettetAv,
        periodeListe = periodeListe
      )
    )
  }

  private fun finnPeriodeTil(periodeTil: LocalDate?, periodeListe: List<Periode>, i: Int): LocalDate? {
    return if (i == periodeListe.size) {
      // Siste element i listen, periodeTil skal ikke justeres
      periodeTil
    } else {
      periodeTil ?: periodeListe[i].fomDato
    }
  }
}