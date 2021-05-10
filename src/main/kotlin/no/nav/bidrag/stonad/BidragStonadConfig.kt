package no.nav.bidrag.stonad

import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
class BidragStonadConfig {

  @Bean
  fun exceptionLogger(): ExceptionLogger {
    return ExceptionLogger(BidragStonad::class.java.simpleName)
  }

  @Bean
  fun correlationIdFilter(): CorrelationIdFilter {
    return CorrelationIdFilter()
  }

  companion object {
    const val LIVE_PROFILE = "live"
  }
}
