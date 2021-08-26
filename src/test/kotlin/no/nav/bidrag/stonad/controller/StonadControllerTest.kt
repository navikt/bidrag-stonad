package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.TestUtil
import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.api.FinnStonadResponse
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.api.NyStonadResponse
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.bidrag.stonad.service.PersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("StonadControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class StonadControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var mottakerIdHistorikkRepository: MottakerIdHistorikkRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null
  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-stonad")
  }

  @Test
  fun `skal opprette ny stonad`() {

    // Oppretter ny forekomst av stønad
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyStonad(),
      HttpMethod.POST,
      byggStonadRequest(),
      NyStonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
    )
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne data for en stonad`() {
    // Oppretter ny forekomst av stonad

      val nyStonadOpprettet = persistenceService.opprettNyStonad(StonadDto(
        stonadType = "BIDRAG",
        sakId = "SAK-001",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        opprettetAvSaksbehandlerId = "X123456",
        endretAvSaksbehandlerId =  "X654321"
      ))

    val periodeListe = listOf(
      PeriodeDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        stonadId = nyStonadOpprettet.stonadId,
        vedtakId = 321,
        periodeGjortUgyldigAvVedtakId = 246,
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
      PeriodeDto(
        periodeFom = LocalDate.parse("2019-07-01"),
        periodeTil = LocalDate.parse("2020-01-01"),
        stonadId = nyStonadOpprettet.stonadId,
        vedtakId = 323,
        periodeGjortUgyldigAvVedtakId = 22,
        belop = BigDecimal.valueOf(3520),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG")
    )
    val periodeDtoListe = ArrayList<PeriodeDto>()
    periodeListe.forEach {
      periodeDtoListe.add(persistenceService.opprettNyPeriode(it))
    }

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonad()}/${nyStonadOpprettet.stonadId}",
      HttpMethod.GET,
      null,
      FinnStonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.stonadType).isEqualTo(nyStonadOpprettet.stonadType) },
      Executable { assertThat(response?.body?.sakId).isEqualTo(nyStonadOpprettet.sakId) },
      Executable { assertThat(response?.body?.skyldnerId).isEqualTo(nyStonadOpprettet.skyldnerId) },
      Executable { assertThat(response?.body?.kravhaverId).isEqualTo(nyStonadOpprettet.kravhaverId) },
      Executable { assertThat(response?.body?.mottakerId).isEqualTo(nyStonadOpprettet.mottakerId) },
      Executable { assertThat(response?.body?.opprettetAvSaksbehandlerId).isEqualTo(nyStonadOpprettet.opprettetAvSaksbehandlerId) },
    )
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal endre mottakerId og opprette historikk`() {

    val nyStonadOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAvSaksbehandlerId = "X123456",
      endretAvSaksbehandlerId =  "X654321"
    ))

    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForEndreMottakerIdStonad(),
      HttpMethod.POST,
      byggEndreMottakerIdRequest(nyStonadOpprettet.stonadId),
      MottakerIdHistorikkDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.mottakerIdEndretFra).isEqualTo("01018211111") },
      Executable { assertThat(response?.body?.mottakerIdEndretTil).isEqualTo("123") },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo("Test") }
    )
    mottakerIdHistorikkRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  private fun byggEndreMottakerIdRequest(stonadId: Int): HttpEntity<EndreMottakerIdRequest> {
    return initHttpEntity(EndreMottakerIdRequest(stonadId, nyMottakerId = "123", saksbehandlerId = "Test"))
  }

  private fun fullUrlForNyStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_NY).toUriString()
  }

  private fun fullUrlForSokStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_SOK).toUriString()
  }

  private fun fullUrlForEndreMottakerIdStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_ENDRE_MOTTAKER_ID).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggStonadRequest(): HttpEntity<NyStonadRequest> {
    return initHttpEntity(TestUtil.byggStonadRequest())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
