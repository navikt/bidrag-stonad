package no.nav.bidrag.stonad.hendelse

import com.fasterxml.jackson.core.JacksonException
import no.nav.bidrag.stonad.LOGGER
import no.nav.bidrag.stonad.SECURE_LOGGER
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import org.springframework.kafka.annotation.KafkaListener

interface VedtakHendelseListener {
    fun lesHendelse(hendelse: String)
}

// sporingsdata fra hendelse json
open class PojoVedtakHendelseListener(
    private val jsonMapperService: JsonMapperService,
    private val behandeHendelseService: BehandleHendelseService,
) : VedtakHendelseListener {
    override fun lesHendelse(hendelse: String) {
        try {
            val vedtakHendelse = jsonMapperService.mapHendelse(hendelse)
            behandeHendelseService.behandleHendelse(vedtakHendelse)
        } catch (e: JacksonException) {
            LOGGER.error(
                "Mapping av hendelse feilet for kafkamelding, se sikker logg for mer info",
            )
            SECURE_LOGGER.error(
                "Mapping av hendelse feilet for kafkamelding: $hendelse",
            )
            throw e
        }
    }
}

open class KafkaVedtakHendelseListener(jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService) :
    PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService) {
    @KafkaListener(groupId = "bidrag-stonad", topics = ["\${TOPIC_VEDTAK}"], errorHandler = "vedtakshendelseErrorHandler")
    override fun lesHendelse(hendelse: String) {
        super.lesHendelse(hendelse)
    }
}
