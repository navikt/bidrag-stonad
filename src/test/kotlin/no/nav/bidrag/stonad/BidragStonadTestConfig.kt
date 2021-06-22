package no.nav.bidrag.stonad

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.hendelse.PojoVedtakHendelseListener
import no.nav.bidrag.stonad.hendelse.VedtakHendelseListener
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import no.nav.security.token.support.test.jersey.TestTokenGeneratorResource
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

@Configuration
@Profile(TEST_PROFILE)
class BidragStonadTestConfig {

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val testTokenGeneratorResource = TestTokenGeneratorResource()
        return "Bearer " + testTokenGeneratorResource.issueToken("localhost-idtoken")
    }

    @Bean
    fun vedtakHendelseListener(
        jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
    ): VedtakHendelseListener = PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService)
}
