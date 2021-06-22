package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
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

open class PojoVedtakHendelseListener(
  private val jsonMapperService: JsonMapperService,
  private val behandeHendelseService: BehandleHendelseService
) : VedtakHendelseListener {

  override fun lesHendelse(hendelse: String) {
    val vedtakHendelse = jsonMapperService.mapHendelse(hendelse)
    behandeHendelseService.behandleHendelse(vedtakHendelse)
  }
}
