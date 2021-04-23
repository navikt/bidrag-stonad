package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.BidragstonadLocal.Companion.TEST_PROFILE
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["springfox.documentation.swagger.web.ApiResourceController"])
@ActiveProfiles(TEST_PROFILE)
@Import(TokenGeneratorConfiguration::class)
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [Bidragstonad::class])])
class BidragstonadLocal {

  companion object {
    const val TEST_PROFILE = "test"
  }
}

fun main(args: Array<String>) {
  val profile = if (args.isEmpty()) TEST_PROFILE else args[0]
  val app = SpringApplication(BidragstonadLocal::class.java)
  app.setAdditionalProfiles(profile)
  app.run(*args)
}
