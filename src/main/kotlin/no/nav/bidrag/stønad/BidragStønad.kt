package no.nav.bidrag.stønad

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJpaRepositories
class BidragStønad

const val ISSUER = "aad"
val SECURE_LOGGER: Logger = LoggerFactory.getLogger("secureLogger")

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) LIVE_PROFILE else args[0]
    val app = SpringApplication(BidragStønad::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
