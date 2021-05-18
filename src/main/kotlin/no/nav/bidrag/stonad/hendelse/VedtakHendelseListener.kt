package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.stonad.service.JsonMapperService

interface VedtakHendelseListener {
  fun lesHendelse(hendelse: String)
}

class KafkaVedtakHendelseListener(
  jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
) : PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService) {

  @KafkaListener(groupId = "bidrag-arbeidsflyt", topics = ["\${TOPIC_JOURNALPOST}"], errorHandler = "hendelseErrorHandler")
  override fun lesHendelse(hendelse: String) {
    super.lesHendelse(hendelse)
  }
}

open class PojoVedtakHendelseListener(
  private val jsonMapperService: JsonMapperService,
  private val behandeHendelseService: BehandleHendelseService
) : VedtakHendelseListener {

  override fun lesHendelse(hendelse: String) {
    val journalpostHendelse = jsonMapperService.mapHendelse(hendelse)
    behandeHendelseService.behandleHendelse(journalpostHendelse)
  }
}