package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragstonadLocal
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.dto.PeriodeDto
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

@DisplayName("PeriodeServiceTest")
@ActiveProfiles(BidragstonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragstonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeServiceTest {

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var stonadsendringService: StonadMottakerIdHistorikkService

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var stonadRepository: stonadRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

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
  fun `skal opprette ny periode`() {

    // Oppretter nytt stonad
    val nyttstonadRequest = NyttstonadRequest(saksbehandlerId = "1111", enhetId = "TEST")
    val nyttstonadOpprettet = stonadService.opprettNyttstonad(nyttstonadRequest)

    // Oppretter ny stonad
    val nyStonadsendringRequest = NyStonadsendringRequest(
      "BIDRAG", nyttstonadOpprettet.stonadId,
      "1111", "1111", "1111", "1111"
    )
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), nyStonadsendringOpprettet.stonadsendringId,
      BigDecimal.valueOf(17), "NOK", "RESULTATKODE_TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.belop).isEqualTo(BigDecimal.valueOf(17)) },
      Executable { assertThat(nyPeriodeOpprettet.valutakode).isEqualTo("NOK") },
      Executable { assertThat(nyPeriodeOpprettet.resultatkode).isEqualTo("RESULTATKODE_TEST") },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(nyttstonadOpprettet.enhetId).isEqualTo("TEST") }

    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne data for en periode`() {
    // Finner data for én periode

    // Oppretter nytt stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", stonadId = nyttstonadOpprettet.stonadId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111"
      ))

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

    val periodeFunnet = periodeService.finnPeriode(nyPeriodeOpprettet.periodeId)

    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.belop).isEqualTo(nyPeriodeOpprettet.belop) },
      Executable { assertThat(periodeFunnet.valutakode).isEqualTo(nyPeriodeOpprettet.valutakode) },
      Executable { assertThat(periodeFunnet.resultatkode).isEqualTo(nyPeriodeOpprettet.resultatkode) }

    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }


  @Test
  fun `skal finne alle perioder for en stonadsendring`() {
    // Finner alle perioder

    // Oppretter nytt stonad
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(stonadDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet1 = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", stonadId = nyttstonadOpprettet1.stonadId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111"
      ))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet2 = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", stonadId = nyttstonadOpprettet2.stonadId, behandlingId = "9999",
        skyldnerId = "9999", kravhaverId = "9999", mottakerId = "9999"
      ))

    // Oppretter nye perioder
    val nyPeriodeDtoListe = mutableListOf<PeriodeDto>()

    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId,
          belop = BigDecimal.valueOf(17.02),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    // Oppretter ny periode
    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId,
          belop = BigDecimal.valueOf(2000.01),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    // Oppretter ny periode som ikke skal bli funnet pga annen stonadsendringId
    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet2.stonadsendringId,
          belop = BigDecimal.valueOf(9999.99),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    val stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId
    val periodeFunnet = periodeService.finnAllePerioderForStonadsendring(stonadsendringId)

    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring.size).isEqualTo(2) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[0].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[1].belop).isEqualTo(BigDecimal.valueOf(2000.01)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[0].resultatkode).isEqualTo(
        "RESULTATKODE_TEST_FLERE_PERIODER") },
      Executable {
      periodeFunnet.allePerioderForStonadsendring.forEachIndexed{ index, periode ->
        assertAll(
          Executable { assertThat(periode.stonadsendringId).isEqualTo(nyPeriodeDtoListe[index].stonadsendringId)},
          Executable { assertThat(periode.periodeId).isEqualTo(nyPeriodeDtoListe[index].periodeId)},
          Executable { assertThat(periode.belop).isEqualTo(nyPeriodeDtoListe[index].belop)}
        )
      }
    }
    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }
  }