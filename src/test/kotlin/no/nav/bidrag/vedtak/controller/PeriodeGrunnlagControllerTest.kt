package no.nav.bidrag.stonad.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.stonad.BidragstonadLocal
import no.nav.bidrag.stonad.BidragstonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.api.AlleGrunnlagForPeriodeResponse
import no.nav.bidrag.stonad.api.AlleStonadsendringerForstonadResponse
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.NyttPeriodeGrunnlagRequest
import no.nav.bidrag.stonad.dto.GrunnlagDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.PeriodeGrunnlagDto
import no.nav.bidrag.stonad.dto.StonadsendringDto
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
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeGrunnlagControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragstonadLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class PeriodeGrunnlagControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

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
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
/*    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()*/
  }


  @Test
  fun `skal opprette nytt periodegrunnlag`() {

    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet.stonadId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111")
    )

    val nyttGrunnlagOpprettet = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttPeriodeGrunnlag(),
      HttpMethod.POST,
      byggRequest(nyPeriodeOpprettet.periodeId, nyttGrunnlagOpprettet.grunnlagId),
      PeriodeGrunnlagDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.periodeId).isEqualTo(nyPeriodeOpprettet.periodeId) },
      Executable { assertThat(response?.body?.grunnlagId).isEqualTo(nyttGrunnlagOpprettet.grunnlagId) },

    )
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
  }

/*
  @Test
  fun `skal finne data for et periodegrunnlag`() {

    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG",
      stonadId = nyttstonadOpprettet.stonadId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111")
    )

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER")
    )

    val nyttGrunnlagOpprettet = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val nyttPeriodeGrunnlagOpprettet = persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        periodeId = nyPeriodeOpprettet.periodeId,
        grunnlagId = nyttGrunnlagOpprettet.stonadId,
        grunnlagValgt = true)
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokPeriodeGrunnlag()}/${nyPeriodeOpprettet.periodeId}&${nyttGrunnlagOpprettet.grunnlagId}",
      HttpMethod.GET,
      null,
      PeriodeGrunnlagDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet.periodeId) },
      Executable { assertThat(response?.body?.grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagId) },
      Executable { assertThat(response?.body?.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagValgt) }
    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
  }
*/

  @Test
  fun `skal finne alle grunnlag for en periode`() {
    // Oppretter ny forekomst av stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye forekomster av stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
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
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER")
    )

    val nyttGrunnlagOpprettet1 = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )
    val nyttGrunnlagOpprettet2 = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Skatt",
        grunnlagInnhold = "10")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val nyttPeriodeGrunnlagOpprettet1 = persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        periodeId = nyPeriodeOpprettet.periodeId,
        grunnlagId = nyttGrunnlagOpprettet1.grunnlagId,
        grunnlagValgt = true)
    )
    // Oppretter ny forekomst av periodeGrunnlag
    val nyttPeriodeGrunnlagOpprettet2 = persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        periodeId = nyPeriodeOpprettet.periodeId,
        grunnlagId = nyttGrunnlagOpprettet2.grunnlagId,
        grunnlagValgt = false)
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokAllePeriodeGrunnlagForPeriode()}/${nyPeriodeOpprettet.periodeId}",
      HttpMethod.GET,
      null,
      no.nav.bidrag.stonad.api.AlleGrunnlagForPeriodeResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode).isNotNull },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![0].periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet1.periodeId) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![1].periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet2.periodeId) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![0].grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet1.grunnlagId) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![1].grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet2.grunnlagId) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![0].grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet1.grunnlagValgt) },
      Executable { assertThat(response?.body?.alleGrunnlagForPeriode!![1].grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet2.grunnlagValgt) },

    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
  }

  private fun fullUrlForNyttPeriodeGrunnlag(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeGrunnlagController.PERIODEGRUNNLAG_NYTT).toUriString()
  }

  private fun fullUrlForSokPeriodeGrunnlag(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeGrunnlagController.PERIODEGRUNNLAG_SOK).toUriString()
  }

  private fun fullUrlForSokAllePeriodeGrunnlagForPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeGrunnlagController.PERIODEGRUNNLAG_SOK_PERIODE).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(periodeId: Int, grunnlagId: Int): HttpEntity<NyttPeriodeGrunnlagRequest> {
    return initHttpEntity(NyttPeriodeGrunnlagRequest(
      periodeId,
      grunnlagId,
      true)
    )
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
