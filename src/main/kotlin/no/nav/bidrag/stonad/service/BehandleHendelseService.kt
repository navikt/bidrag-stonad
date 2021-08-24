package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.FinnStonadResponse
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.hendelse.StonadType
import no.nav.bidrag.stonad.hendelse.VedtakHendelse
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
    LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

    when (vedtakHendelse.hentStonadType()) {
      StonadType.BIDRAG, StonadType.FORSKUDD -> behandleVedtakHendelse(vedtakHendelse)
      StonadType.NO_SUPPORT -> LOGGER.warn("bidrag-stønad støtter ikke hendelsen '${vedtakHendelse.stonadType}'")
    }
  }

  private fun behandleVedtakHendelse(vedtakHendelse: VedtakHendelse) {
    val eksisterendeStonad = stonadService.finnStonad(
      vedtakHendelse.stonadType,
      vedtakHendelse.skyldnerId,
      vedtakHendelse.kravhaverId
    )
    if (eksisterendeStonad != null) {
      // Mottatt Hendelse skal oppdatere eksisterende stønad
      endreStonad(eksisterendeStonad, vedtakHendelse)
    } else {
      opprettStonad(vedtakHendelse)
    }
  }

  private fun endreStonad(eksisterendeStonad: FinnStonadResponse, vedtakHendelse: VedtakHendelse) {
    val periodeListe = mutableListOf<NyPeriodeRequest>()
    vedtakHendelse.periodeListe.forEach {
      periodeListe.add(
        NyPeriodeRequest(
          periodeFom = it.periodeFom,
          periodeTil = it.periodeTil,
          vedtakId = vedtakHendelse.vedtakId,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
    }

    val oppdatertStonad =
      NyStonadRequest(
        stonadType = vedtakHendelse.stonadType,
        sakId = vedtakHendelse.sakId,
        skyldnerId = vedtakHendelse.skyldnerId,
        kravhaverId = vedtakHendelse.kravhaverId,
        mottakerId = vedtakHendelse.mottakerId,
        opprettetAvSaksbehandlerId = vedtakHendelse.opprettetAvSaksbehandlerId,
        endretAvSaksbehandlerId = vedtakHendelse.opprettetAvSaksbehandlerId,
        periodeListe = periodeListe
      )

    stonadService.endreStonad(eksisterendeStonad, oppdatertStonad)

  }

  private fun opprettStonad(vedtakHendelse: VedtakHendelse) {

    val periodeListe = mutableListOf<NyPeriodeRequest>()
    vedtakHendelse.periodeListe.forEach {
      periodeListe.add(
        NyPeriodeRequest(
          periodeFom = it.periodeFom,
          periodeTil = it.periodeTil,
          vedtakId = vedtakHendelse.vedtakId,
          periodeGjortUgyldigAvVedtakId = null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
    }

    stonadService.opprettStonad(
      NyStonadRequest(
        stonadType = vedtakHendelse.stonadType,
        sakId = vedtakHendelse.sakId,
        skyldnerId = vedtakHendelse.skyldnerId,
        kravhaverId = vedtakHendelse.kravhaverId,
        mottakerId = vedtakHendelse.mottakerId,
        opprettetAvSaksbehandlerId = vedtakHendelse.opprettetAvSaksbehandlerId,
        periodeListe = periodeListe
      )
    )

  }
}