package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.TestUtil
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.api.NyttKomplettstonadRequest
import no.nav.bidrag.stonad.api.NyttstonadResponse
import no.nav.bidrag.stonad.dto.stonadDto
import no.nav.bidrag.stonad.persistence.repository.GrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadsendringRepository
import no.nav.bidrag.stonad.persistence.repository.stonadRepository
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

@DisplayName("stonadControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class stonadControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var stonadRepository: stonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-stonad")
  }

  @Test
  fun `skal opprette nytt stonad`() {
    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttstonad(),
      HttpMethod.POST,
      byggRequest(),
      stonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.enhetId).isEqualTo("1111") },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo("TEST") }
    )
  }

  @Test
  fun `skal finne data for ett stonad`() {
    // Oppretter ny forekomst
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(enhetId = "1111", saksbehandlerId = "TEST"))

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokstonad()}/${nyttstonadOpprettet.stonadId}",
      HttpMethod.GET,
      null,
      stonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyttstonadOpprettet.stonadId) },
      Executable { assertThat(response?.body?.enhetId).isEqualTo(nyttstonadOpprettet.enhetId) },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo(nyttstonadOpprettet.saksbehandlerId) }
    )
  }

  @Test
  fun `skal finne data for alle stonad`() {
    // Oppretter nye forekomster
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(stonadDto(enhetId = "1111", saksbehandlerId = "TEST"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(stonadDto(enhetId = "2222", saksbehandlerId = "TEST"))

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      fullUrlForSokstonad(),
      HttpMethod.GET,
      null,
      no.nav.bidrag.stonad.api.AllestonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.allestonad).isNotNull },
      Executable { assertThat(response?.body?.allestonad!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.allestonad!![0].stonadId).isEqualTo(nyttstonadOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.allestonad!![0].enhetId).isEqualTo(nyttstonadOpprettet1.enhetId) },
      Executable { assertThat(response?.body?.allestonad!![0].saksbehandlerId).isEqualTo(nyttstonadOpprettet1.saksbehandlerId) },
      Executable { assertThat(response?.body?.allestonad!![1].stonadId).isEqualTo(nyttstonadOpprettet2.stonadId) },
      Executable { assertThat(response?.body?.allestonad!![1].enhetId).isEqualTo(nyttstonadOpprettet2.enhetId) },
      Executable { assertThat(response?.body?.allestonad!![1].saksbehandlerId).isEqualTo(nyttstonadOpprettet2.saksbehandlerId) }
    )
  }

  @Test
  fun `skal opprette nytt komplett stonad`() {
    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttKomplettstonad(),
      HttpMethod.POST,
      byggKomplettstonadRequest(),
      NyttstonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() }
    )
  }

  private fun fullUrlForNyttstonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + stonadController.stonad_NY).toUriString()
  }

  private fun fullUrlForNyttKomplettstonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + stonadController.stonad_NY_KOMPLETT).toUriString()
  }

  private fun fullUrlForSokstonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + stonadController.stonad_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(): HttpEntity<NyttstonadRequest> {
    return initHttpEntity(NyttstonadRequest(saksbehandlerId = "TEST", enhetId = "1111"))
  }

  private fun byggKomplettstonadRequest(): HttpEntity<NyttKomplettstonadRequest> {
    return initHttpEntity(TestUtil.byggKomplettstonadRequest())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
