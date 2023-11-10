package no.nav.bidrag.stønad.hendelse

import no.nav.bidrag.stønad.service.BehandleHendelseService
import no.nav.bidrag.stønad.service.JsonMapperService
import org.springframework.kafka.annotation.KafkaListener

interface VedtakHendelseListener {
    fun lesHendelse(hendelse: String)
}

open class KafkaVedtakHendelseListener(
    jsonMapperService: JsonMapperService,
    behandeHendelseService: BehandleHendelseService,
) : PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService) {

    @KafkaListener(groupId = "bidrag-stonad", topics = ["\${TOPIC_VEDTAK}"], errorHandler = "vedtakshendelseErrorHandler")
    override fun lesHendelse(hendelse: String) {
        super.lesHendelse(hendelse)
    }
}

// sporingsdata fra hendelse json
open class PojoVedtakHendelseListener(
    private val jsonMapperService: JsonMapperService,
    private val behandeHendelseService: BehandleHendelseService,
) : VedtakHendelseListener {

    override fun lesHendelse(hendelse: String) {
        val vedtakHendelse = jsonMapperService.mapHendelse(hendelse)
        behandeHendelseService.behandleHendelse(vedtakHendelse)
    }
}
