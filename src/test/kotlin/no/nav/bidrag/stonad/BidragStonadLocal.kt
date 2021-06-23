package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootApplication
@ActiveProfiles(TEST_PROFILE)
@Import(TokenGeneratorConfiguration::class)
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragStonad::class])])
class BidragStonadLocal {

  companion object {
    const val TEST_PROFILE = "test"
  }
}

fun main(args: Array<String>) {
  val profile = if (args.isEmpty()) TEST_PROFILE else args[0]
  val app = SpringApplication(BidragStonadLocal::class.java)
  app.setAdditionalProfiles(profile)
  app.run(*args)
}
