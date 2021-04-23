package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.BidragStonadConfig.Companion.LIVE_PROFILE
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BidragStonad

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) LIVE_PROFILE else args[0]
    val app = SpringApplication(BidragStonad::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}