package no.nav.bidrag.stonad.controller

import no.nav.bidrag.behandling.felles.dto.stonad.AlleMottakerIdHistorikkForStonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.BidragStonadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
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
import org.springframework.boot.web.server.LocalServerPort
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

@DisplayName("MottakerIdHistorikkControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class MottakerIdHistorikkControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var mottakerIdHistorikkRepository: MottakerIdHistorikkRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    mottakerIdHistorikkRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port")
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne alle endringer av mottaker-id for en stønad`() {
    // Oppretter nye forekomster

    val periodeListe = listOf(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        vedtakId = 321,
        periodeGjortUgyldigAvVedtakId = 246,
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
    )


    val nyStonadOpprettetStonadId = persistenceService.opprettNyStonad(OpprettStonadRequestDto(
      stonadType = StonadType.BIDRAG,
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAv = "X123456",
      periodeListe
    ))

    persistenceService.opprettNyMottakerIdHistorikk(
      EndreMottakerIdRequestDto(nyStonadOpprettetStonadId, "654", "X123456")
    )

    persistenceService.opprettNyMottakerIdHistorikk(
      EndreMottakerIdRequestDto(nyStonadOpprettetStonadId,"876", "X654321")
    )

    val mottakerIdHistorikkListe = persistenceService.hentAlleEndringerAvMottakerIdForStonad(nyStonadOpprettetStonadId)

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "/mottakeridhistorikk/sok/${nyStonadOpprettetStonadId}",
      HttpMethod.GET,
      null,
      AlleMottakerIdHistorikkForStonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].stonadId)
        .isEqualTo(mottakerIdHistorikkListe?.get(0)?.stonadId) },

      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretFra)
        .isEqualTo(mottakerIdHistorikkListe?.get(0)?.mottakerIdEndretFra) },

      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretTil)
        .isEqualTo(mottakerIdHistorikkListe?.get(0)?.mottakerIdEndretTil) },

      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].opprettetAv)
        .isEqualTo(mottakerIdHistorikkListe?.get(0)?.opprettetAv) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].stonadId)
        .isEqualTo(mottakerIdHistorikkListe?.get(1)?.stonadId) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].mottakerIdEndretFra)
        .isEqualTo(mottakerIdHistorikkListe?.get(1)?.mottakerIdEndretFra) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].mottakerIdEndretTil)
        .isEqualTo(mottakerIdHistorikkListe?.get(1)?.mottakerIdEndretTil) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].opprettetAv)
        .isEqualTo(mottakerIdHistorikkListe?.get(1)?.opprettetAv) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].opprettetAv)
        .isEqualTo(mottakerIdHistorikkListe?.get(1)?.opprettetAv) }
    )
  }

  private fun fullUrlForSokAlleMottakerIdHistorikkForStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + MottakerIdHistorikkController.MOTTAKER_ID_HISTORIKK_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port"
  }

  private fun byggRequest(stonadId: Int): HttpEntity<EndreMottakerIdRequestDto> {
    return initHttpEntity(EndreMottakerIdRequestDto(stonadId, nyMottakerId = "123", opprettetAv = "Test"))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
