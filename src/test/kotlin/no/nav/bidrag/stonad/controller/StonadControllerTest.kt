/*
package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
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

@DisplayName("StonadsendringControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class StonadControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var stonadsendringRepository: StonadRepository

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
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-stonad")
  }

  @Test
  fun `skal opprette ny stonadsendring`() {
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stønadsendring
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyStonadsendring(),
      HttpMethod.POST,
      byggRequest(nyttstonadOpprettet.stonadId),
      StonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyttstonadOpprettet.stonadId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo("1111") }
    )
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne data for en stonadsendring`() {
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        stonadId = nyttstonadOpprettet.stonadId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonadsendring()}/${nyStonadsendringOpprettet.stonadId}",
      HttpMethod.GET,
      null,
      StonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyStonadsendringOpprettet.stonadId) },
      Executable { assertThat(response?.body?.stonadType).isEqualTo(nyStonadsendringOpprettet.stonadType) },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyStonadsendringOpprettet.stonadId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo(nyStonadsendringOpprettet.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne alle stonadsendringer for et stonad`() {
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter nye forekomster av stønadsendring
    val nyStonadsendringOpprettet1 = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        stonadId = nyttstonadOpprettet1.stonadId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    val nyStonadsendringOpprettet2 = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        stonadId = nyttstonadOpprettet1.stonadId,
        behandlingId = "2222",
        skyldnerId = "2222",
        kravhaverId = "2222",
        mottakerId = "2222"
      )
    )

    // Stonadsendring som ikke skal legges med i resultatet
    persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        stonadId = nyttstonadOpprettet2.stonadId,
        behandlingId = "9999",
        skyldnerId = "9999",
        kravhaverId = "9999",
        mottakerId = "9999"
      )
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonadsendringForstonad()}/${nyttstonadOpprettet1.stonadId}",
      HttpMethod.GET,
      null,
      no.nav.bidrag.stonad.api.AlleStonadsendringerForstonadResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad).isNotNull },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![0].stonadId).isEqualTo(nyStonadsendringOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![0].stonadType).isEqualTo(nyStonadsendringOpprettet1.stonadType) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![0].stonadId).isEqualTo(nyStonadsendringOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![0].behandlingId).isEqualTo(nyStonadsendringOpprettet1.behandlingId) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![1].stonadId).isEqualTo(nyStonadsendringOpprettet2.stonadId) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![1].stonadType).isEqualTo(nyStonadsendringOpprettet2.stonadType) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![1].stonadId).isEqualTo(nyStonadsendringOpprettet2.stonadId) },
      Executable { assertThat(response?.body?.alleStonadsendringerForstonad!![1].behandlingId).isEqualTo(nyStonadsendringOpprettet2.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  private fun fullUrlForNyStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_ENDRE_MOTTAKER_ID).toUriString()
  }

  private fun fullUrlForSokStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_SOK).toUriString()
  }

  private fun fullUrlForSokStonadsendringForstonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONADSENDRING_SOK_stonad).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(stonadId: Int): HttpEntity<NyStonadRequest> {
    return initHttpEntity(NyStonadRequest(
      "BIDRAG",
      stonadId,
      "1111",
      "1111",
      "1111",
      "1111"
    ))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
*/
