package no.nav.bidrag.stonad.controller

import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.BidragStonadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.TestUtil
import no.nav.bidrag.stonad.bo.toPeriodeBo
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.bidrag.stonad.service.PersistenceService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("StonadControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class StonadControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port")
  }


  @Test
  fun `skal finne data for en stonad`() {
    // Oppretter ny forekomst av stonad

    val periodeListe = listOf(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        vedtakId = 321,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = 246,
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-07-01"),
        periodeTil = LocalDate.parse("2020-01-01"),
        vedtakId = 323,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = 22,
        belop = BigDecimal.valueOf(3520),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG")
    )

    val stonadOpprettetStonadId = persistenceService.opprettStonad(
      OpprettStonadRequestDto(
        type = StonadType.BIDRAG,
        sakId = "SAK-001",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        indeksreguleringAar = "2024",
        innkreving = Innkreving.JA,
        opprettetAv = "X123456",
        periodeListe = periodeListe
      )
    )

    periodeListe.forEach {
      persistenceService.opprettPeriode(it.toPeriodeBo(), stonadOpprettetStonadId)
    }

    val stonadOpprettet = persistenceService.hentStonadFraId(stonadOpprettetStonadId)

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "/hent-stonad/",
      HttpMethod.POST,
      byggStonadResponse(),
      StonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.type.toString()).isEqualTo(stonadOpprettet?.type) },
      Executable { assertThat(response?.body?.sakId).isEqualTo(stonadOpprettet?.sakId) },
      Executable { assertThat(response?.body?.skyldnerId).isEqualTo(stonadOpprettet?.skyldnerId) },
      Executable { assertThat(response?.body?.kravhaverId).isEqualTo(stonadOpprettet?.kravhaverId) },
      Executable { assertThat(response?.body?.mottakerId).isEqualTo(stonadOpprettet?.mottakerId) },
      Executable { assertThat(response?.body?.opprettetAv).isEqualTo(stonadOpprettet?.opprettetAv) },
      Executable { assertThat(response?.body?.innkreving.toString()).isEqualTo(stonadOpprettet?.innkreving) },
    )
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }


  private fun fullUrlForSokStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.HENT_STONAD).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port"
  }

  private fun byggStonadRequest(): HttpEntity<OpprettStonadRequestDto> {
    return initHttpEntity(TestUtil.byggStonadRequest())
  }

  private fun byggStonadResponse(): HttpEntity<StonadDto> {
    return initHttpEntity(TestUtil.byggStonadDto())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
