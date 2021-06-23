package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener

interface VedtakHendelseListener {
  fun lesHendelse(hendelse: String)
}

class KafkaVedtakHendelseListener(
  jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
) : PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService) {

  @KafkaListener(groupId = "bidrag-stonad", topics = ["\${TOPIC_VEDTAK}"], errorHandler = "vedtakshendelseErrorHandler")
  override fun lesHendelse(hendelse: String) {
    super.lesHendelse(hendelse)
  }
}

// sporingsdata fra hendelse json
private const val CORRELATION_ID = "correlationId"
private val LOGGER = LoggerFactory.getLogger(PojoVedtakHendelseListener::class.java)

open class PojoVedtakHendelseListener(
  private val jsonMapperService: JsonMapperService,
  private val behandeHendelseService: BehandleHendelseService
) : VedtakHendelseListener {

  override fun lesHendelse(hendelse: String) {
    leggTilCorrelationId(hendelse)
    val vedtakHendelse = jsonMapperService.mapHendelse(hendelse)
    behandeHendelseService.behandleHendelse(vedtakHendelse)
    MDC.clear()
  }

  private fun leggTilCorrelationId(hendelse: String) {
    try {
      val jsonNode = jsonMapperService.readTree(hendelse)
      val correlationIdJsonNode = jsonNode["sporing"]?.get(CORRELATION_ID)

      if (correlationIdJsonNode == null) {
        val unknown = "unknown-${System.currentTimeMillis().toString(16)}"
        LOGGER.warn("Unable to find correlation Id in '${hendelse.trim(' ')}', using '$unknown'")
        MDC.put(CORRELATION_ID, unknown)
      } else {
        val correlationId = CorrelationId.existing(correlationIdJsonNode.asText())
        MDC.put(CORRELATION_ID, correlationId.get())
      }
    } catch (e: Exception) {
      LOGGER.error("Unable to parse '$hendelse': ${e.javaClass.simpleName}: ${e.message}")
    }
  }
}
