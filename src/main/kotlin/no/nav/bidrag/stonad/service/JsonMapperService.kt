package no.nav.bidrag.stonad.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.stonad.hendelse.VedtakHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(JsonMapperService::class.java)

@Service
class JsonMapperService(private val objectMapper: ObjectMapper) {
    fun mapHendelse(hendelse: String): VedtakHendelse {
        return try {
            objectMapper.readValue(hendelse, VedtakHendelse::class.java)
        } finally {
            LOGGER.debug("Leser hendelse: {}", hendelse)
        }
    }

    fun readTree(hendelse: String) = objectMapper.readTree(hendelse)
}
