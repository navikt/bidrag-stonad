package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.BidragstonadLocal.Companion.TEST_PROFILE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragstonadLocal::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("Bidragstonad")
class BidragstonadApplicationTest {

  @Test
  fun `skal laste spring-context`() {
  }
}
