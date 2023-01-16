package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
      behandleVedtakHendelse(stonadsendring, vedtakHendelse.type, vedtakHendelse.id, vedtakHendelse.opprettetAv)
    }
  }

  private fun behandleVedtakHendelse(stonadsendring: Stonadsendring, vedtakType: VedtakType, vedtakId: Int, opprettetAv: String) {
    val eksisterendeStonad = stonadService.hentStonad(
      HentStonadRequest(stonadsendring.type, stonadsendring.sakId, stonadsendring.skyldnerId, stonadsendring.kravhaverId))

    if (eksisterendeStonad != null) {
      if (vedtakType == VedtakType.ENDRING_MOTTAKER) {
        // Mottatt hendelse skal oppdatere mottakerId for alle stønader i stonadsendringListe. Ingen perioder skal oppdateres.
        persistenceService.endreMottakerId(eksisterendeStonad.stonadId, stonadsendring.mottakerId, opprettetAv)
      } else {
        // Mottatt Hendelse skal oppdatere eksisterende stønad
        endreStonad(eksisterendeStonad, stonadsendring, vedtakId, opprettetAv)
      }
    } else {
      opprettStonad(stonadsendring, vedtakId, opprettetAv)
    }
  }

  private fun endreStonad(eksisterendeStonad: StonadDto, stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String) {
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    stonadsendring.periodeListe.forEach {
      periodeListe.add(
        OpprettStonadPeriodeRequestDto(
          periodeFom = it.fomDato,
          periodeTil = it.tilDato,
          vedtakId = vedtakId,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
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

    stonadService.endreStonad(eksisterendeStonad, oppdatertStonad)

  }

  private fun opprettStonad(stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String) {

    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    stonadsendring.periodeListe.forEach {
      periodeListe.add(
        OpprettStonadPeriodeRequestDto(
          periodeFom = it.fomDato,
          periodeTil = it.tilDato,
          vedtakId = vedtakId,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
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
}