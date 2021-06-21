package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.hendelse.StonadType
import no.nav.bidrag.stonad.hendelse.VedtakHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(DefaultBehandleHendelseService::class.java)

interface BehandleHendelseService {
    fun behandleHendelse(vedtakHendelse: VedtakHendelse)
}

@Service
class DefaultBehandleHendelseService() : BehandleHendelseService {

    override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
        LOGGER.info("Behandler journalpostHendelse: $vedtakHendelse")

        when (vedtakHendelse.hentStonadType()) {
            StonadType.BARNEBIDRAG -> behandleBarnebidrag(vedtakHendelse)
            StonadType.FORSKUDD -> behandleForskudd(vedtakHendelse)
            StonadType.NO_SUPPORT -> LOGGER.warn("bidrag-stønad støtter ikke hendelsen '${vedtakHendelse.stonadType}'")
        }
    }

    private fun behandleBarnebidrag(vedtakHendelse: VedtakHendelse) {
    }

    private fun behandleForskudd(vedtakHendelse: VedtakHendelse) {
    }

}
