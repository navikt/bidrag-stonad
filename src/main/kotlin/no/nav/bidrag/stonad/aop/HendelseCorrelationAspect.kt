package no.nav.bidrag.stonad.aop

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.CorrelationId.Companion.CORRELATION_ID_HEADER
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.*

private const val CORRELATION_ID = "correlationId"

private val LOGGER = KotlinLogging.logger {}

@Component
@Aspect
class HendelseCorrelationAspect(private val objectMapper: ObjectMapper) {
    @Before(value = "execution(* no.nav.bidrag.stonad.hendelse.VedtakHendelseListener.lesHendelse(..)) && args(hendelse)")
    fun leggSporingFraVedtakHendelseTilMDC(joinPoint: JoinPoint, hendelse: String) {
        hentSporingFraHendelse(hendelse)?.let {
            val correlationId = CorrelationId.existing(it)
            MDC.put(CORRELATION_ID_HEADER, correlationId.get())
        } ?: run {
            val tilfeldigVerdi = UUID.randomUUID().toString().subSequence(0, 8)
            val korrelasjonsId = "${tilfeldigVerdi}_prossesserVedtakHendelse"
            MDC.put(CORRELATION_ID_HEADER, CorrelationId.existing(korrelasjonsId).get())
        }
    }

    private fun hentSporingFraHendelse(hendelse: String): String? = try {
        val jsonNode = objectMapper.readTree(hendelse)
        val correlationId = jsonNode["sporingsdata"]?.get(CORRELATION_ID)?.asText()
        if (correlationId.isNullOrEmpty()) null else correlationId
    } catch (e: Exception) {
        LOGGER.error("Det skjedde en feil ved konvertering av melding fra hendelse: ", e)
        null
    }

    @After(value = "execution(* no.nav.bidrag.stonad.hendelse.VedtakHendelseListener.*(..))")
    fun clearCorrelationIdFromScheduler(joinPoint: JoinPoint) {
        MDC.clear()
    }
}
