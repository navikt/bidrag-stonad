package no.nav.bidrag.stonad

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.LOCAL_PROFILE
import no.nav.bidrag.stonad.BidragStonadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.hendelse.PojoVedtakHendelseListener
import no.nav.bidrag.stonad.hendelse.VedtakHendelseListener
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-stonad", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")]
)

@Profile(TEST_PROFILE, LOCAL_PROFILE)
class BidragStonadTestConfig {

    @Autowired
    private var mockOAuth2Server: MockOAuth2Server? = null

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val token = mockOAuth2Server?.issueToken(ISSUER, "aud-localhost", "aud-localhost")
        return "Bearer " + token?.serialize()
    }

    @Bean
    fun vedtakHendelseListener(
        jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
    ): VedtakHendelseListener = PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService)
}
