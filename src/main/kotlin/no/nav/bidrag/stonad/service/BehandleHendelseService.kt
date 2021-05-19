package no.nav.bidrag.stonad.service

import no.nav.bidrag.arbeidsflyt.dto.OppgaveSokRequest
import no.nav.bidrag.arbeidsflyt.hendelse.JournalpostHendelse
import no.nav.bidrag.stonad.hendelse.StonadType
import no.nav.bidrag.stonad.hendelse.VedtakHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

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
            StonadType.SAERTILSKUDD -> behandleSaertilskudd(vedtakHendelse)
            StonadType.ENDRE_MOTTAKERID -> behandleEndreMottakerId(vedtakHendelse)
            StonadType.NO_SUPPORT -> LOGGER.warn("bidrag-stønad støtter ikke hendelsen '${vedtakHendelse.hendelse}'")
        }
    }

    private fun behandleBarnebidrag(vedtakHendelse: VedtakHendelse) {
        if (vedtakHendelse.erBytteTilInterntFagomrade()) {
            LOGGER.info("Hendelsen ${vedtakHendelse.hendelse} er vedtak om barnebidrag")
        } else {
            behandleSaertilskudd(vedtakHendelse)
        }
    }

    private fun behandleForskudd(vedtakHendelse: VedtakHendelse) {
        val fagomrade = vedtakHendelse.hentFagomradeFraId()

        val overforOppgaverForPrefixetId = CompletableFuture.supplyAsync {
            oppgaveService.overforOppgaver(OppgaveSokRequest(vedtakHendelse.journalpostId, fagomrade), vedtakHendelse)
        }

        val overforOppgaverUtenPrefixetId = CompletableFuture.supplyAsync {
            oppgaveService.overforOppgaver(OppgaveSokRequest(vedtakHendelse.hentJournalpostIdUtenPrefix(), fagomrade), vedtakHendelse)
        }

        CompletableFuture.allOf(overforOppgaverForPrefixetId, overforOppgaverUtenPrefixetId)
            .get() // overfører oppgaver tilhørende journalpost (med og uten prefix)
    }

    private fun behandleSaertilskudd(journalpostHendelse: JournalpostHendelse) {
        val fagomrade = journalpostHendelse.hentFagomradeFraId()

        val ferdigstillOppgaverForPrefixetId = CompletableFuture.supplyAsync {
            oppgaveService.ferdigstillOppgaver(OppgaveSokRequest(journalpostHendelse.journalpostId, fagomrade), journalpostHendelse)
        }

        val ferdigstillOppgaverUtenPrefixetId = CompletableFuture.supplyAsync {
            oppgaveService.ferdigstillOppgaver(OppgaveSokRequest(journalpostHendelse.hentJournalpostIdUtenPrefix(), fagomrade), journalpostHendelse)
        }

        CompletableFuture.allOf(ferdigstillOppgaverForPrefixetId, ferdigstillOppgaverUtenPrefixetId)
            .get() // ferdigstiller oppgaver tilhørende journalpost med og uten prefix på id
    }
}
