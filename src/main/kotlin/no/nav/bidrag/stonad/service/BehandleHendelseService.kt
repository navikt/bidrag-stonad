package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val LOGGER = LoggerFactory.getLogger(DefaultBehandleHendelseService::class.java)

interface BehandleHendelseService {
  fun behandleHendelse(vedtakHendelse: VedtakHendelse)
}

@Service
@Transactional
class DefaultBehandleHendelseService(
    private val stonadService: StonadService,
    private val persistenceService: PersistenceService
) : BehandleHendelseService {

  override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
    SECURE_LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

    vedtakHendelse.stonadsendringListe?.forEach() { stonadsendring ->
      behandleVedtakHendelse(stonadsendring, vedtakHendelse.type, vedtakHendelse.id, vedtakHendelse.opprettetAv, vedtakHendelse.vedtakTidspunkt)
    }
  }

  private fun behandleVedtakHendelse(
      stonadsendring: Stonadsendring, vedtakType: VedtakType, vedtakId: Int, opprettetAv: String, vedtakTidspunkt: LocalDateTime) {
//    Sjekker om stønad skal oppdateres
    if (stonadsendring.endring && stonadsendring.innkreving == Innkreving.JA) {
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
        //Stønaden finnes ikke fra , hvis det er forsøkt endret mottaker for stønad som ikke finnes så skal det logges, men ikke feile.
        if (vedtakType == VedtakType.ENDRING_MOTTAKER) {
          SECURE_LOGGER.info("Mottaker forsøkt endret for stønad som ikke finnes $vedtakId")
        } else {
          opprettStonad(stonadsendring, vedtakId, opprettetAv, vedtakTidspunkt)
        }
      }
    } else {
      SECURE_LOGGER.info("Stønad ikke oppdatert pga innkreving = nei eller endring = false: $vedtakId")
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
    hendelsePeriodeListe.forEach { periode ->
      // Kun perioder med beløp skal lagres
      if (periode.belop != null) {
        periodeListe.add(
            OpprettStonadPeriodeRequestDto(
                periodeFom = periode.fomDato,
                periodeTil = finnPeriodeTil(periode.tilDato, hendelsePeriodeListe, i),
                vedtakId = vedtakId,
                gyldigFra = vedtakTidspunkt,
                gyldigTil = null,
                periodeGjortUgyldigAvVedtakId = null,
                belop = periode.belop,
                valutakode = periode.valutakode,
                resultatkode = periode.resultatkode
            )
        )
      }
      i++
    }

    // Hvis periodelisten er tom (kun perioder med beløp = null) så skal stønaden ikke opprettes
    if (periodeListe.isNotEmpty()) {
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