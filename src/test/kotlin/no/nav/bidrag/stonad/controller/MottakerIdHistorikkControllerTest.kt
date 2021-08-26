package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse
import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
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

@DisplayName("MottakerIdHistorikkControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
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

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    mottakerIdHistorikkRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-stonad")
  }

/*  @Test
  fun `skal opprette ny MottakerIdHistorikk`() {

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
      fullUrlForNyMottakerIdHistorikk(),
      HttpMethod.POST,
      byggRequest(nyStonadOpprettet.stonadId),
      MottakerIdHistorikkDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.mottakerIdEndretFra).isEqualTo("123") },
      Executable { assertThat(response?.body?.mottakerIdEndretTil).isEqualTo("321") },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo("Test") }
    )
    mottakerIdHistorikkRepository.deleteAll()
    stonadRepository.deleteAll()
  }*/

  @Test
  fun `skal finne alle endringer av mottaker-id for en stonad`() {
    // Oppretter nye forekomster
    val nyStonadOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAvSaksbehandlerId = "X123456",
      endretAvSaksbehandlerId =  "X654321"
    ))

    val nyMottakerIdHistorikkOpprettet1 = persistenceService.opprettNyMottakerIdHistorikk(
      EndreMottakerIdRequest(nyStonadOpprettet.stonadId, "654", "X123456"))

    val nyMottakerIdHistorikkOpprettet2 = persistenceService.opprettNyMottakerIdHistorikk(
      EndreMottakerIdRequest(nyStonadOpprettet.stonadId,"876", "X654321"))

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokAlleMottakerIdHistorikkForStonad()}/${nyStonadOpprettet.stonadId}",
      HttpMethod.GET,
      null,
      AlleMottakerIdHistorikkForStonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].stonadId).isEqualTo(nyMottakerIdHistorikkOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretFra).isEqualTo(nyMottakerIdHistorikkOpprettet1.mottakerIdEndretFra) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretTil).isEqualTo(nyMottakerIdHistorikkOpprettet1.mottakerIdEndretTil) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![0].saksbehandlerId).isEqualTo(nyMottakerIdHistorikkOpprettet1.saksbehandlerId) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].stonadId).isEqualTo(nyMottakerIdHistorikkOpprettet2.stonadId) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].mottakerIdEndretFra).isEqualTo(nyMottakerIdHistorikkOpprettet2.mottakerIdEndretFra) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].mottakerIdEndretTil).isEqualTo(nyMottakerIdHistorikkOpprettet2.mottakerIdEndretTil) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].saksbehandlerId).isEqualTo(nyMottakerIdHistorikkOpprettet2.saksbehandlerId) },
      Executable { assertThat(response?.body?.alleMottakerIdHistorikkForStonad!![1].saksbehandlerId).isEqualTo(nyMottakerIdHistorikkOpprettet2.saksbehandlerId) }
    )
  }

  private fun fullUrlForSokAlleMottakerIdHistorikkForStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + MottakerIdHistorikkController.MOTTAKER_ID_HISTORIKK_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(stonadId: Int): HttpEntity<EndreMottakerIdRequest> {
    return initHttpEntity(EndreMottakerIdRequest(stonadId, nyMottakerId = "123", saksbehandlerId = "Test"))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
