package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragstonadLocal
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.NyttGrunnlagRequest
import no.nav.bidrag.stonad.api.NyttPeriodeGrunnlagRequest
import no.nav.bidrag.stonad.api.NyttstonadRequest
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeGrunnlagServiceTest")
@ActiveProfiles(BidragstonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragstonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeGrunnlagServiceTest {

  @Autowired
  private lateinit var periodeGrunnlagService: PeriodeGrunnlagService

  @Autowired
  private lateinit var grunnlagService: GrunnlagService

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var stonadsendringService: StonadMottakerIdHistorikkService

  @Autowired
  private lateinit var stonadService: StonadService

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
  fun `skal opprette nytt periodeGrunnlag`() {
    // Oppretter nytt stonad
    val nyttstonadRequest = NyttstonadRequest("TEST", "1111")
    val nyttstonadOpprettet = stonadService.opprettNyttstonad(nyttstonadRequest)

    // Oppretter ny stønadsendring
    val nyStonadsendringRequest = NyStonadsendringRequest(
      "BIDRAG",
      nyttstonadOpprettet.stonadId,
      "1111",
      "1111",
      "1111",
      "1111"
    )
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    // Oppretter nytt grunnlag
    val nyttGrunnlagRequest = NyttGrunnlagRequest(
      grunnlagReferanse = "",
      stonadId = nyttstonadOpprettet.stonadId,
      grunnlagType = "Beregnet Inntekt",
      grunnlagInnhold = "100")

    val grunnlagOpprettet = grunnlagService.opprettNyttGrunnlag(nyttGrunnlagRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), nyStonadsendringOpprettet.stonadsendringId,
      BigDecimal.valueOf(17), "NOK", "RESULTATKODE_TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    // Oppretter nytt periodegrunnlag
    val nyttPeriodeGrunnlagRequest = NyttPeriodeGrunnlagRequest(
      nyPeriodeOpprettet.periodeId,
      grunnlagOpprettet.grunnlagId,
      true
    )
    val nyttPeriodeGrunnlagOpprettet = periodeGrunnlagService.opprettNyttPeriodeGrunnlag(nyttPeriodeGrunnlagRequest)

    assertAll(
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.periodeId).isEqualTo(nyttPeriodeGrunnlagRequest.periodeId) },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.grunnlagId).isEqualTo(nyttPeriodeGrunnlagRequest.grunnlagId) },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagRequest.grunnlagValgt) }
    )
  }

  @Test
  fun `skal finne data for et periodegrunnlag`() {
    // Oppretter nytt stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stønadsendring
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

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST"
      )
    )

    // Oppretter nytt grunnlag
    val nyttGrunnlagOpprettet = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )

    // Oppretter nytt periodegrunnlag
    val nyttPeriodeGrunnlagOpprettet = persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        nyPeriodeOpprettet.periodeId,
        nyttGrunnlagOpprettet.grunnlagId,
        true)
    )

    // Finner periodegrunnlag som akkurat ble opprettet
    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(
      nyttPeriodeGrunnlagOpprettet.periodeId, nyttPeriodeGrunnlagOpprettet.grunnlagId)

    assertAll(
      Executable { assertThat(periodeGrunnlagFunnet).isNotNull() },
      Executable { assertThat(periodeGrunnlagFunnet.periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet.periodeId) },
      Executable { assertThat(periodeGrunnlagFunnet.grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagId) },
      Executable { assertThat(periodeGrunnlagFunnet.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagValgt) }
    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne alle periodegrunnlag for en periode`() {

    // Oppretter nytt stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

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

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST"
      )
    )

    // Oppretter nye grunnlag
    val nyttGrunnlagDtoListe = mutableListOf<GrunnlagDto>()

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        stonadId = nyttstonadOpprettet.stonadId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
      )
    )

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettNyttGrunnlag(
        GrunnlagDto(
          grunnlagReferanse = "",
          stonadId = nyttstonadOpprettet.stonadId,
          grunnlagType = "Beregnet Skatt",
          grunnlagInnhold = "10")
      )
    )

    // Oppretter nye periodegrunnlag
    val nyttPeriodegrunnlagtoListe = mutableListOf<PeriodeGrunnlagDto>()

    nyttPeriodegrunnlagtoListe.add(
      persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        nyPeriodeOpprettet.periodeId,
        nyttGrunnlagDtoListe[0].grunnlagId,
        true)
      )
    )

    nyttPeriodegrunnlagtoListe.add(
      persistenceService.opprettNyttPeriodeGrunnlag(
        PeriodeGrunnlagDto(
          nyPeriodeOpprettet.periodeId,
          nyttGrunnlagDtoListe[1].grunnlagId,
          true)
      )
    )

    // Finner begge periodegrunnlagene som akkurat ble opprettet
    val periodeId = nyPeriodeOpprettet.periodeId
    val periodegrunnlagFunnet = periodeGrunnlagService.hentAlleGrunnlagForPeriode(periodeId)

    assertAll(
      Executable { assertThat(periodegrunnlagFunnet).isNotNull() },
      Executable { assertThat(periodegrunnlagFunnet.alleGrunnlagForPeriode).isNotNull() },
      Executable { assertThat(periodegrunnlagFunnet.alleGrunnlagForPeriode.size).isEqualTo(2) },
      Executable {
        periodegrunnlagFunnet.alleGrunnlagForPeriode.forEachIndexed { index, periodeGrunnlag ->
          assertAll(
            Executable { assertThat(periodeGrunnlag.periodeId).isEqualTo(nyttPeriodegrunnlagtoListe[index].periodeId) },
            Executable { assertThat(periodeGrunnlag.grunnlagId).isEqualTo(nyttPeriodegrunnlagtoListe[index].grunnlagId) },
            Executable { assertThat(periodeGrunnlag.grunnlagValgt).isEqualTo(nyttPeriodegrunnlagtoListe[index].grunnlagValgt) },
          )
        }
      }
    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }
}
