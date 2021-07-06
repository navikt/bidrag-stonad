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
  private val periodeService: PeriodeService,
  val persistenceService: PersistenceService
) : BehandleHendelseService {

  override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
    LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

    when (vedtakHendelse.hentStonadType()) {
      StonadType.BIDRAG, StonadType.FORSKUDD -> behandleVedtak(vedtakHendelse)
      StonadType.NO_SUPPORT -> LOGGER.warn("bidrag-stønad støtter ikke hendelsen '${vedtakHendelse.stonadType}'")
    }
  }

  private fun behandleVedtak(vedtakHendelse: VedtakHendelse) {
    val eksisterendeStonad = stonadService.finnStonad(
      vedtakHendelse.stonadType,
      vedtakHendelse.kravhaverId,
      vedtakHendelse.skyldnerId
    )
    if (eksisterendeStonad != null) {
      // Mottatt Hendelse skal oppdatere eksisterende stønad
      oppdaterStonad(vedtakHendelse, eksisterendeStonad)
    } else {
      opprettNyStonad(vedtakHendelse)
    }
  }

  private fun oppdaterStonad(
    vedtakHendelse: VedtakHendelse,
    eksisterendeStonad: FinnStonadResponse
  ) {
    persistenceService.settAllePerioderSomOverlapperForStonadSomUgyldig(
      vedtakHendelse.vedtakId,
      eksisterendeStonad.periodeListe[0].vedtakId
    )


  }

  private fun opprettNyStonad(vedtakHendelse: VedtakHendelse) {


    val nyStonad = stonadService.opprettStonad(
      NyStonadRequest(
        stonadType = vedtakHendelse.stonadType,
        sakId = vedtakHendelse.sakId,
        skyldnerId = vedtakHendelse.skyldnerId,
        kravhaverId = vedtakHendelse.kravhaverId,
        mottakerId = vedtakHendelse.mottakerId,
        opprettetAvSaksbehandlerId = vedtakHendelse.opprettetAvSaksbehandlerId,
        endretAvSaksbehandlerId = vedtakHendelse.endretAvSaksbehandlerId
      )
    )

    vedtakHendelse.periodeListe.forEach {
      periodeService.opprettNyPeriode(
        NyPeriodeRequest(
          periodeFom = it.periodeFom,
          periodeTil = it.periodeTil,
          nyStonad.stonadId,
          vedtakHendelse.vedtakId,
          null,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
    }
  }
}