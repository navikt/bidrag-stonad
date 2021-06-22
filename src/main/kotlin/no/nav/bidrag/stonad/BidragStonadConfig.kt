package no.nav.bidrag.stonad

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.stonad.hendelse.KafkaVedtakHendelseListener
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(info = Info(title = "bidrag-stonad", version = "v1"), security = [SecurityRequirement(name = "bearer-key")])
@EnableJwtTokenValidation(ignore = ["org.springframework"])
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP
)
class BidragStonadConfig {

    @Bean
    fun exceptionLogger(): ExceptionLogger {
        return ExceptionLogger(BidragStonad::class.java.simpleName)
    }

    @Bean
    fun correlationIdFilter(): CorrelationIdFilter {
        return CorrelationIdFilter()
    }
}

@Configuration
@Profile(LIVE_PROFILE)
class KafkaConfig {
  @Bean
  fun vedtakHendelseListener(
    jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
  ) = KafkaVedtakHendelseListener(jsonMapperService, behandeHendelseService)
}
