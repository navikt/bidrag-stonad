package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.StonadType
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
    LOGGER.info("Behandler vedtakHendelse for vedtakid: ${vedtakHendelse.vedtakId}")
    SECURE_LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

    vedtakHendelse.stonadsendringListe?.forEach() { stonadsendring ->
      behandleVedtakHendelse(stonadsendring, vedtakHendelse.vedtakId, vedtakHendelse.opprettetAv)
    }
  }

  private fun behandleVedtakHendelse(stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String) {
    val eksisterendeStonad = stonadService.hentStonad(
      stonadsendring.stonadType.toString(),
      stonadsendring.skyldnerId,
      stonadsendring.kravhaverId,
      stonadsendring.sakId
    )

    if (eksisterendeStonad != null) {
      // Mottatt Hendelse skal oppdatere eksisterende stønad
      endreStonad(eksisterendeStonad, stonadsendring, vedtakId, opprettetAv)
    } else {
      opprettStonad(stonadsendring, vedtakId, opprettetAv)
    }
  }

  private fun endreStonad(eksisterendeStonad: StonadDto, stonadsendring: Stonadsendring, vedtakId: Int, opprettetAv: String) {
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    stonadsendring.periodeListe.forEach {
      periodeListe.add(
        OpprettStonadPeriodeRequestDto(
          periodeFom = it.periodeFomDato,
          periodeTil = it.periodeTilDato,
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
        stonadType = stonadsendring.stonadType,
        sakId = stonadsendring.sakId,
        skyldnerId = stonadsendring.skyldnerId,
        kravhaverId = stonadsendring.kravhaverId,
        mottakerId = stonadsendring.mottakerId,
        indeksreguleringAar = stonadsendring.indeksreguleringAar,
        opphortFra = stonadsendring.opphortFra,
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
          periodeFom = it.periodeFomDato,
          periodeTil = it.periodeTilDato,
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
        stonadType = stonadsendring.stonadType,
        sakId = stonadsendring.sakId,
        skyldnerId = stonadsendring.skyldnerId,
        kravhaverId = stonadsendring.kravhaverId,
        mottakerId = stonadsendring.mottakerId,
        indeksreguleringAar = stonadsendring.indeksreguleringAar,
        opphortFra = stonadsendring.opphortFra,
        opprettetAv = opprettetAv,
        periodeListe = periodeListe
      )
    )

  }
}