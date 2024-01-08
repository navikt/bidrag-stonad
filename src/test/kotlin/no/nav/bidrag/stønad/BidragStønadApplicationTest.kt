package no.nav.bidrag.stønad

import no.nav.bidrag.stønad.BidragStønadTest.Companion.TEST_PROFILE
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragStønadTest::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragStønad")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class BidragStønadApplicationTest {
    @Test
    fun `skal laste spring-context`() {
    }
}
