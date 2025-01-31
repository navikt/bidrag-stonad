package no.nav.bidrag.stønad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.BidragStønadTest
import no.nav.bidrag.stønad.BidragStønadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stønad.TestUtil
import no.nav.bidrag.stønad.bo.toPeriodeBo
import no.nav.bidrag.stønad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stønad.persistence.repository.StønadRepository
import no.nav.bidrag.stønad.service.PersistenceService
import no.nav.bidrag.transport.behandling.stonad.request.LøpendeBidragssakerRequest
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadMedPeriodeBeløpResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto as OpprettStønadsperiodeRequestDto1

@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStønadTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class StønadControllerTest {
    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var stønadRepository: StønadRepository

    @Autowired
    private lateinit var persistenceService: PersistenceService

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun `init`() {
        // Sletter alle forekomster
        periodeRepository.deleteAll()
        stønadRepository.deleteAll()
    }

    @Test
    fun `skal mappe til context path med random port`() {
        assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port")
    }

    @Test
    fun `skal finne data for en stønad`() {
        // Oppretter ny forekomst av stønad

        val periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto1(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 321,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = 246,
                    beløp = BigDecimal.valueOf(3490),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
                OpprettStønadsperiodeRequestDto1(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                    vedtaksid = 323,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = 22,
                    beløp = BigDecimal.valueOf(3520),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            )

        val stønadOpprettetStønadsid =
            persistenceService.opprettStønad(
                OpprettStønadRequestDto(
                    type = Stønadstype.BIDRAG,
                    sak = Saksnummer("SAK-001"),
                    skyldner = Personident("01018011111"),
                    kravhaver = Personident("01010511111"),
                    mottaker = Personident("01018211111"),
                    førsteIndeksreguleringsår = 2024,
                    innkreving = Innkrevingstype.MED_INNKREVING,
                    opprettetAv = "X123456",
                    periodeListe = periodeListe,
                ),
            )

        periodeListe.forEach {
            persistenceService.opprettPeriode(it.toPeriodeBo(), stønadOpprettetStønadsid)
        }

        // Henter forekomst
        val response =
            securedTestRestTemplate.postForEntity<StønadDto>(
                "/hent-stonad/",
                byggStønadRequest(),
            )

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isNotNull },
        )
        periodeRepository.deleteAll()
        stønadRepository.deleteAll()
    }

    @Test
    fun `skal finne stønad med periodebeløp`() {
        // Oppretter ny forekomst av stønad

        val periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto1(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 321,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = 246,
                    beløp = BigDecimal.valueOf(3490),
                    valutakode = "DKK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
                OpprettStønadsperiodeRequestDto1(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                    vedtaksid = 323,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = 22,
                    beløp = BigDecimal.valueOf(3520),
                    valutakode = "DKK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            )

        val stønadOpprettetStønadsid =
            persistenceService.opprettStønad(
                OpprettStønadRequestDto(
                    type = Stønadstype.BIDRAG,
                    sak = Saksnummer("SAK-001"),
                    skyldner = Personident("01018011111"),
                    kravhaver = Personident("01010511111"),
                    mottaker = Personident("01018211111"),
                    førsteIndeksreguleringsår = 2024,
                    innkreving = Innkrevingstype.MED_INNKREVING,
                    opprettetAv = "X123456",
                    periodeListe = periodeListe,
                ),
            )

        periodeListe.forEach {
            persistenceService.opprettPeriode(it.toPeriodeBo(), stønadOpprettetStønadsid)
        }

        // Henter forekomst
        val response =
            securedTestRestTemplate.postForEntity<StønadMedPeriodeBeløpResponse>(
                "/hent-stonad-periodebeløp/",
                byggStønadRequest(),
            )

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isNotNull },
        )
        periodeRepository.deleteAll()
        stønadRepository.deleteAll()
    }

    private fun fullUrlForSøkStønad(): String = UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StønadController.HENT_STØNAD).toUriString()

    private fun makeFullContextPath(): String = "http://localhost:$port"

    private fun byggStønadRequest(): HttpEntity<OpprettStønadRequestDto> = initHttpEntity(TestUtil.byggStonadRequest())

    private fun byggLøpendeBidragssakerRequest(): HttpEntity<LøpendeBidragssakerRequest> = initHttpEntity(TestUtil.byggLøpendeBidragssakerRequest())

    private fun byggStønadResponse(): HttpEntity<StønadDto> = initHttpEntity(TestUtil.byggStønadDto())

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
