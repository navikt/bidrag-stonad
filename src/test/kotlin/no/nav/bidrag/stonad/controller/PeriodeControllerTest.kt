package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
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
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class PeriodeControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var stonadRepository: stonadRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-stonad")
  }


  @Test
  fun `skal opprette ny periode`() {

    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet.stonadId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111"
    )
    )

    // Oppretter ny forekomst av periode
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyPeriode(),
      HttpMethod.POST,
      byggRequest(nyStonadsendringOpprettet.stonadId),
      PeriodeDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyStonadsendringOpprettet.stonadId) },
      Executable { assertThat(response?.body?.belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(response?.body?.resultatkode).isEqualTo("RESULTATKODE_TEST") }

    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()

  }

  @Test
  fun `skal finne data for en periode`(){
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet.stonadId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111"
    )
    )

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadId = nyStonadsendringOpprettet.stonadId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokPeriode()}/${nyPeriodeOpprettet.periodeId}",
      HttpMethod.GET,
      null,
      PeriodeDto::class.java)


    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.periodeId).isEqualTo(nyPeriodeOpprettet.periodeId) },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyPeriodeOpprettet.stonadId) },
      Executable { assertThat(response?.body?.belop).isEqualTo(nyPeriodeOpprettet.belop) },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyPeriodeOpprettet.stonadId) }
    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()

  }

  @Test
  fun `skal finne alle perioder for stonadsendring`(){
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(MottakerIdHistorikkDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet1 = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet1.stonadId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111"
    )
    )

    val nyStonadsendringOpprettet2 = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet2.stonadId,
      behandlingId = "9999",
      skyldnerId = "9999",
      kravhaverId = "9999",
      mottakerId = "9999"
    )
    )

    // Oppretter nye forekomster av periode
    val nyPeriodeOpprettet1 = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadId = nyStonadsendringOpprettet1.stonadId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
      )
    )

    val nyPeriodeOpprettet2 = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadId = nyStonadsendringOpprettet1.stonadId,
        belop = BigDecimal.valueOf(2000.02),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
      )
    )

    // Opprettet periode som ikke skal returneres i resultatet
    persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadId = nyStonadsendringOpprettet2.stonadId,
        belop = BigDecimal.valueOf(9999.99),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokPerioderForStonadsendring()}/${nyStonadsendringOpprettet1.stonadId}",
      HttpMethod.GET,
      null,
      no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse::class.java)


    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![0].periodeId).isEqualTo(nyPeriodeOpprettet1.periodeId) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![0].stonadsendringId).isEqualTo(nyPeriodeOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![1].belop).isEqualTo(nyPeriodeOpprettet2.belop) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![1].stonadsendringId).isEqualTo(nyPeriodeOpprettet2.stonadId) }
    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()

  }



  private fun fullUrlForNyPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_NY).toUriString()
  }

  private fun fullUrlForSokPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_SOK).toUriString()
  }

  private fun fullUrlForSokPerioderForStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_SOK_STONADSENDRING).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(stonadsendringId: Int): HttpEntity<NyPeriodeRequest> {
    return initHttpEntity(NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), stonadsendringId, BigDecimal.valueOf(17.01), "NOK", "RESULTATKODE_TEST"
    ))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
