package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.FinnStonadResponse
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
    private val stonadService: StonadService, val persistenceService: PersistenceService
) : BehandleHendelseService {

    override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
        LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

        when (vedtakHendelse.hentStonadType()) {
            StonadType.BIDRAG -> behandleBarnebidrag(vedtakHendelse)
            StonadType.FORSKUDD -> behandleForskudd(vedtakHendelse)
            StonadType.NO_SUPPORT -> LOGGER.warn("bidrag-stønad støtter ikke hendelsen '${vedtakHendelse.stonadType}'")
        }
    }

    private fun behandleBarnebidrag(vedtakHendelse: VedtakHendelse) {
        val eksisterendeStonad = stonadService.finnStonad(vedtakHendelse.stonadType, vedtakHendelse.kravhaverId, vedtakHendelse.skyldnerId)
        if (eksisterendeStonad != null) {
            // Mottatt Hendelse skal oppdatere eksisterende stønad
            oppdaterStonad(vedtakHendelse, eksisterendeStonad)
        } else {
            opprettNyStonad(vedtakHendelse)
        }
    }

    private fun behandleForskudd(vedtakHendelse: VedtakHendelse) {
        stonadService.finnStonad(vedtakHendelse.stonadType, vedtakHendelse.kravhaverId, vedtakHendelse.skyldnerId)
    }

    private fun oppdaterStonad(vedtakHendelse: VedtakHendelse, eksisterendeStonad: FinnStonadResponse) {
        persistenceService.settAllePerioderForStonadSomUgyldig(vedtakHendelse.vedtakId, eksisterendeStonad.vedtakId)


    }

    private fun opprettNyStonad(vedtakHendelse: VedtakHendelse) {

    }

}
