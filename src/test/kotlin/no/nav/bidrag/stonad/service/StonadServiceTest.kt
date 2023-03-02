package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadHistoriskRequest
import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("stonadServiceTest")
@ActiveProfiles(BidragStonadTest.TEST_PROFILE)
@SpringBootTest(
  classes = [BidragStonadTest::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class StonadServiceTest {

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette ny stønad`() {
    // Oppretter ny stonad
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2021-02-01"),
        periodeTil = LocalDate.parse("2021-03-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "Alles gut"
      )
    )

    val opprettStonadRequest = OpprettStonadRequestDto(
      StonadType.BIDRAG, "SAK-001", "Skyldner123",
      "Kravhaver123", "MottakerId123","2024", Innkreving.JA, "R153961", periodeListe
    )

    val nyStonadOpprettet = stonadService.opprettStonad(opprettStonadRequest)

    assertAll(
      Executable { assertThat(nyStonadOpprettet).isNotNull() },
    )
  }

  @Test
  // Returnerer stønad og alle perioder som ikke er markert som ugyldige
  @Suppress("NonAsciiCharacters")
  fun `skal finne alle gyldige perioder for en stønad`() {
    // Oppretter ny stonad
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2021-02-01"),
        periodeTil = LocalDate.parse("2021-03-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "Alles gut"
      )
    )
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2021-03-01"),
        periodeTil = LocalDate.parse("2021-04-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = 1,
        belop = BigDecimal.valueOf(17.02),
        valutakode = "NOK",
        resultatkode = "Alles gut"
      )
    )
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2021-03-01"),
        periodeTil = LocalDate.parse("2021-04-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(5000.02),
        valutakode = "NOK",
        resultatkode = "Ny periode lagt til"
      )
    )
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2021-04-01"),
        periodeTil = LocalDate.parse("2021-05-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(17.03),
        valutakode = "NOK",
        resultatkode = "Alles gut"
      )
    )

    val opprettStonadRequest = OpprettStonadRequestDto(
      StonadType.BIDRAG, "SAK-001", "Skyldner123",
      "Kravhaver123", "MottakerId123", "2024",Innkreving.JA, "R153961",
      periodeListe
    )

    stonadService.opprettStonad(opprettStonadRequest)

    val opprettetStonad = stonadService.hentStonad(
      HentStonadRequest(opprettStonadRequest.type, opprettStonadRequest.sakId,
      opprettStonadRequest.skyldnerId, opprettStonadRequest.kravhaverId))

    assertAll(
      Executable { assertThat(opprettetStonad).isNotNull() },
      Executable { assertThat(opprettetStonad!!.periodeListe.size).isEqualTo(3) },
      Executable { assertThat(opprettetStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-02-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(opprettetStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.02)) },
      Executable { assertThat(opprettetStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(opprettetStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      )
  }


  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne alle perioder for en stønad, også ugyldiggjorte - Ugyldiggjorte kommer etter gyldige perioder`() {
    // Oppretter ny stonad
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-02-01"), periodeTil = LocalDate.parse("2021-03-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-03-01"), periodeTil = LocalDate.parse("2021-04-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = 1, belop = BigDecimal.valueOf(17.02), valutakode = "NOK",
        resultatkode = "Alles gut"))
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-03-01"), periodeTil = LocalDate.parse("2021-04-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.02), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-04-01"), periodeTil = LocalDate.parse("2021-05-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.03), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val opprettStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    stonadService.opprettStonad(opprettStonadRequest)

    val funnetStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(opprettStonadRequest.type.toString(),
      opprettStonadRequest.skyldnerId, opprettStonadRequest.kravhaverId, opprettStonadRequest.sakId)

    assertAll(
      Executable { assertThat(funnetStonad).isNotNull() },
      Executable { assertThat(funnetStonad!!.periodeListe.size).isEqualTo(4) },
      Executable { assertThat(funnetStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-02-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },
      Executable { assertThat(funnetStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },

      Executable { assertThat(funnetStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },
      Executable { assertThat(funnetStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.02)) },

      Executable { assertThat(funnetStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isNull() },
      Executable { assertThat(funnetStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.03)) },

      Executable { assertThat(funnetStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[3].periodeTil).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { assertThat(funnetStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtakId).isEqualTo(1) },
      Executable { assertThat(funnetStonad!!.periodeListe[3].belop).isEqualTo(BigDecimal.valueOf(17.02)) },

      )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne stønad fra sammensatt nøkkel`() {
    // Oppretter ny stønad

    val periodeListe = listOf(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(1),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
    )

    val nyStonadOpprettetStonadId = persistenceService.opprettStonad(
      OpprettStonadRequestDto(
        StonadType.BIDRAG, "SAK-001", "Skyldner123",
    "Kravhaver123", "MottakerId123",  "2024", Innkreving.JA, "R153961", periodeListe)
    )

    val nyStonadOpprettet = persistenceService.hentStonadFraId(nyStonadOpprettetStonadId)

    // Finner stønaden som akkurat ble opprettet
    val stonadFunnet = stonadService.hentStonad(HentStonadRequest(
      StonadType.valueOf(nyStonadOpprettet!!.type),
      nyStonadOpprettet.sakId,
      nyStonadOpprettet.skyldnerId,
      nyStonadOpprettet.kravhaverId)
    )

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne stønad fra generert id`() {
    // Oppretter ny stonad
    val periodeListe = listOf(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        vedtakId = 1,
        gyldigFra = LocalDateTime.now(),
        gyldigTil = null,
        periodeGjortUgyldigAvVedtakId = null,
        belop = BigDecimal.valueOf(1),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
    )

    val nyStonadOpprettetStonadId = persistenceService.opprettStonad(
      OpprettStonadRequestDto(
        StonadType.BIDRAG, "SAK-001", "Skyldner123",
        "Kravhaver123", "MottakerId123", "2024", Innkreving.JA,"R153961",
        periodeListe)
    )

    // Finner stønaden som akkurat ble opprettet
    val stonadFunnet = stonadService.hentStonadFraId(nyStonadOpprettetStonadId)

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
    )
  }


  @Test
  @Suppress("NonAsciiCharacters")
  // endrer eksisterende stønad og ugyldiggjør perioder som har blitt endret i nytt vedtak
  fun `skal endre eksisterende stønad`() {
    // Oppretter først stønaden som skal endres etterpå
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2021-03-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-03-01"), periodeTil = LocalDate.parse("2021-07-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.02), valutakode = "NOK",
        resultatkode = "Alles gut"))
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-07-01"), periodeTil = LocalDate.parse("2021-12-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.03), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val originalStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA,"R153961", periodeListe)

    stonadService.opprettStonad(originalStonadRequest)
    val originalStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(
      originalStonadRequest.type.toString(), originalStonadRequest.skyldnerId, originalStonadRequest.kravhaverId, originalStonadRequest.sakId)

    // Oppretter så ny request som skal oppdatere eksisterende stønad
    val endretStonadPeriodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.01), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-06-01"), periodeTil = LocalDate.parse("2021-08-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.02), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-08-01"), periodeTil = LocalDate.parse("2021-10-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.03), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))

    val endretStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", endretStonadPeriodeListe)

    stonadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
    val endretStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(endretStonadRequest.type.toString(),
      endretStonadRequest.skyldnerId, endretStonadRequest.kravhaverId, endretStonadRequest.sakId)

    assertAll(
      // Perioder sorteres på periodeGjortUgyldigAvVedtakId så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtakId kommer sist.
      Executable { assertThat(endretStonad).isNotNull() },
      Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(8) },

      // Første periode er før perioder for nytt vedtak, blir ikke endret
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },

      // Avkortet utgave av ugyldiggjort periode med til-dato lik fom-dato for nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },

      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(5000.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isNull() },
      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].belop).isEqualTo(BigDecimal.valueOf(5000.02)) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtakId).isNull() },
      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[4].periodeFom).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[4].periodeTil).isEqualTo(LocalDate.parse("2021-10-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[4].belop).isEqualTo(BigDecimal.valueOf(5000.03)) },
      Executable { assertThat(endretStonad!!.periodeListe[4].periodeGjortUgyldigAvVedtakId).isNull() },

      // Avkortet utgave av ugyldiggjort periode med fom-dato lik til-dato for nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[5].periodeFom).isEqualTo(LocalDate.parse("2021-10-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[5].periodeTil).isEqualTo(LocalDate.parse("2021-12-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[5].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      Executable { assertThat(endretStonad!!.periodeListe[5].periodeGjortUgyldigAvVedtakId).isNull() },

      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[6].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[6].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[6].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
      Executable { assertThat(endretStonad!!.periodeListe[6].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[7].periodeFom).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[7].periodeTil).isEqualTo(LocalDate.parse("2021-12-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[7].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      Executable { assertThat(endretStonad!!.periodeListe[7].periodeGjortUgyldigAvVedtakId).isEqualTo(2) }
      )

  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
  fun `Test på splitt av perioder med vedtak med periode midt i eksisterende stønad`() {
    // Oppretter først stønaden som skal endres etterpå
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2022-01-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val originalStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    stonadService.opprettStonad(originalStonadRequest)
    val originalStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(originalStonadRequest.type.toString(),
      originalStonadRequest.skyldnerId, originalStonadRequest.kravhaverId, originalStonadRequest.sakId)

    // Oppretter så ny request som skal oppdatere eksisterende stønad
    val endretStonadPeriodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.01), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))

    val endretStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", endretStonadPeriodeListe)

    stonadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
    val endretStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(endretStonadRequest.type.toString(),
      endretStonadRequest.skyldnerId, endretStonadRequest.kravhaverId, endretStonadRequest.sakId)

    assertAll(
      // Perioder sorteres på periodeGjortUgyldigAvVedtakId så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtakId kommer sist.
      Executable { assertThat(endretStonad).isNotNull() },
      Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(4) },

      // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
      // Første periode
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },

      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },

      // Siste periode fra eksisterende stønad
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isNull() },

      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

    )

  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
  fun `Test med null i tildato på ny vedtaksperiode`() {
    // Oppretter først stønaden som skal endres etterpå
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2022-01-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val originalStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    stonadService.opprettStonad(originalStonadRequest)
    val originalStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(originalStonadRequest.type.toString(),
      originalStonadRequest.skyldnerId, originalStonadRequest.kravhaverId, originalStonadRequest.sakId)

    // Oppretter så ny request som skal oppdatere eksisterende stønad
    val endretStonadPeriodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = null, vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.01), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))

    val endretStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024",Innkreving.JA,  "R153961", endretStonadPeriodeListe)

    stonadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
    val endretStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(endretStonadRequest.type.toString(),
      endretStonadRequest.skyldnerId, endretStonadRequest.kravhaverId, endretStonadRequest.sakId)

    assertAll(
      // Perioder sorteres på periodeGjortUgyldigAvVedtakId så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtakId kommer sist.
      Executable { assertThat(endretStonad).isNotNull() },
      Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(3) },

      // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
      // Første periode
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },

      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeTil).isNull() },
      Executable { assertThat(endretStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },

      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

      )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
  fun `Test med null i tildato på eksisterende stønadsperiode`() {
    // Oppretter først stønaden som skal endres etterpå
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = null, vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val originalStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    stonadService.opprettStonad(originalStonadRequest)
    val originalStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(originalStonadRequest.type.toString(),
      originalStonadRequest.skyldnerId, originalStonadRequest.kravhaverId, originalStonadRequest.sakId)

    // Oppretter så ny request som skal oppdatere eksisterende stønad
    val endretStonadPeriodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.01), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))

    val endretStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", endretStonadPeriodeListe)

    stonadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
    val endretStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(endretStonadRequest.type.toString(),
      endretStonadRequest.skyldnerId, endretStonadRequest.kravhaverId, endretStonadRequest.sakId)

    assertAll(
      // Perioder sorteres på periodeGjortUgyldigAvVedtakId så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtakId kommer sist.
      Executable { assertThat(endretStonad).isNotNull() },
      Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(4) },

      // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
      // Første periode
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },

      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },

      // Siste periode fra eksisterende stønad
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeTil).isNull() },
      Executable { assertThat(endretStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isNull() },

      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeTil).isNull() },
      Executable { assertThat(endretStonad!!.periodeListe[3].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

      )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Alle perioder i eksisterende stønad som befinner seg innenfor fra- og tildato for nytt vedtak skal erstattes selv om det finnes en identisk periode i det nye vedtaket.
  fun `Test med like perioder og endret beløp i én periode`() {
    // Oppretter først stønaden som skal endres etterpå
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2021-05-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))

    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.02), valutakode = "NOK",
        resultatkode = "Alles gut"))

    periodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-06-01"), periodeTil = null, vedtakId = 1,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.03), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val originalStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    stonadService.opprettStonad(originalStonadRequest)
    val originalStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(originalStonadRequest.type.toString(),
      originalStonadRequest.skyldnerId, originalStonadRequest.kravhaverId, originalStonadRequest.sakId)

    // Oppretter så ny request som skal oppdatere eksisterende stønad
    val endretStonadPeriodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()

    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2021-05-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
        resultatkode = "Alles gut"))

    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-05-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(5000.01), valutakode = "NOK",
        resultatkode = "Ny periode lagt til"))

    endretStonadPeriodeListe.add(
      OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-06-01"), periodeTil = null, vedtakId = 2,
        gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.03), valutakode = "NOK",
        resultatkode = "Alles gut"))

    val endretStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
      "MottakerId123", "2024", Innkreving.JA, "R153961", endretStonadPeriodeListe)

    stonadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
    val endretStonad = stonadService.hentStonadInkludertUgyldiggjortePerioder(endretStonadRequest.type.toString(),
      endretStonadRequest.skyldnerId, endretStonadRequest.kravhaverId, endretStonadRequest.sakId)

    assertAll(
      // Perioder sorteres på periodeGjortUgyldigAvVedtakId så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtakId kommer sist.
      Executable { assertThat(endretStonad).isNotNull() },
      Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(6) },

      // Alle perioder for eksisterende stønad ugyldigjøres selv om noen av periodene er identiske
      // Første periode
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[0].vedtakId).isEqualTo(2) },
      Executable { assertThat(endretStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },

      // Periode fra nytt vedtak
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[1].vedtakId).isEqualTo(2) },
      Executable { assertThat(endretStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },

      // Siste periode fra eksisterende stønad
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeTil).isNull() },
      Executable { assertThat(endretStonad!!.periodeListe[2].vedtakId).isEqualTo(2) },
      Executable { assertThat(endretStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isNull() },

      // Perioden overlapper med nytt vedtak, settes til ugyldig
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeTil).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[3].vedtakId).isEqualTo(1) },
      Executable { assertThat(endretStonad!!.periodeListe[3].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

      Executable { assertThat(endretStonad!!.periodeListe[4].periodeFom).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[4].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[4].vedtakId).isEqualTo(1) },
      Executable { assertThat(endretStonad!!.periodeListe[4].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
      Executable { assertThat(endretStonad!!.periodeListe[4].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

      Executable { assertThat(endretStonad!!.periodeListe[5].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(endretStonad!!.periodeListe[5].periodeTil).isNull() },
      Executable { assertThat(endretStonad!!.periodeListe[5].vedtakId).isEqualTo(1) },
      Executable { assertThat(endretStonad!!.periodeListe[5].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      Executable { assertThat(endretStonad!!.periodeListe[5].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

      )
  }


  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne historiske perioder for en stønad`() {
    // Oppretter ny stønad
    val periodeListe = mutableListOf<OpprettStonadPeriodeRequestDto>()
    //Legger først til periode som ikke skal returneres
    periodeListe.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2022-01-01"), vedtakId = 2,
            gyldigFra = LocalDateTime.parse("2021-01-17T17:17:17.179121094"), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
            resultatkode = "Alles gut"))
    periodeListe.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-01-01"), periodeTil = LocalDate.parse("2021-04-01"), vedtakId = 1,
            gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"), gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"), periodeGjortUgyldigAvVedtakId = 2, belop = BigDecimal.valueOf(17.02), valutakode = "NOK",
            resultatkode = "Alles gut"))
    periodeListe.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-04-01"), periodeTil = LocalDate.parse("2021-06-01"), vedtakId = 1,
            gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"), gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"), periodeGjortUgyldigAvVedtakId = 2, belop = BigDecimal.valueOf(5000.02), valutakode = "NOK",
            resultatkode = "Ny periode lagt til"))
    periodeListe.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.parse("2021-06-01"), periodeTil = LocalDate.parse("2022-01-01"), vedtakId = 1,
            gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"), gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"), periodeGjortUgyldigAvVedtakId = 2, belop = BigDecimal.valueOf(17.03), valutakode = "NOK",
            resultatkode = "Alles gut"))

    val opprettStonadRequest = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner123","Kravhaver123",
        "MottakerId123", "2024", Innkreving.JA, "R153961", periodeListe)

    val stonadId = stonadService.opprettStonad(opprettStonadRequest)

    val funnetStonad = stonadService.hentStonadHistorisk(HentStonadHistoriskRequest(opprettStonadRequest.type,
        opprettStonadRequest.sakId, opprettStonadRequest.skyldnerId, opprettStonadRequest.kravhaverId, LocalDateTime.parse("2020-12-31T23:00:00.169121094")))

    assertAll(
        Executable { assertThat(funnetStonad).isNotNull() },
        Executable { assertThat(funnetStonad!!.periodeListe.size).isEqualTo(3) },
        Executable { assertThat(funnetStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-04-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
        Executable { assertThat(funnetStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.02)) },

        Executable { assertThat(funnetStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-04-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
        Executable { assertThat(funnetStonad!!.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(5000.02)) },

        Executable { assertThat(funnetStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
        Executable { assertThat(funnetStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
        Executable { assertThat(funnetStonad!!.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.03)) }

        )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal finne alle stønader for angitt sakId`() {
    // Oppretter ny stønad
    val periodeListe1 = mutableListOf<OpprettStonadPeriodeRequestDto>()
    val periodeListe2 = mutableListOf<OpprettStonadPeriodeRequestDto>()
    val periodeListe3 = mutableListOf<OpprettStonadPeriodeRequestDto>()
    // Oppretter stønad 1
    periodeListe1.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.now(), periodeTil = LocalDate.now().plusDays(30), vedtakId = 1,
            gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(17.01), valutakode = "NOK",
            resultatkode = "Alles gut"))
    periodeListe1.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.now(), periodeTil = LocalDate.now().plusDays(30), vedtakId = 1,
            gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(100.01), valutakode = "NOK",
            resultatkode = "Alles gut"))
    val opprettStonadRequest1 = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-001", "Skyldner001","Kravhaver001",
        "MottakerId001", "2024", Innkreving.JA, "R153961", periodeListe1)
    stonadService.opprettStonad(opprettStonadRequest1)

    // Oppretter stønad 2, ligger på en annen sak og skal ikke hentes
    periodeListe2.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.now(), periodeTil = LocalDate.now().plusDays(30), vedtakId = 2,
            gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(998.02), valutakode = "NOK",
            resultatkode = "Alles gut"))
    val opprettStonadRequest2 = OpprettStonadRequestDto(StonadType.BIDRAG, "SAK-002", "Skyldner002","Kravhaver002",
        "MottakerId002", "2024", Innkreving.JA, "R153961", periodeListe2)
    stonadService.opprettStonad(opprettStonadRequest2)

    // Oppretter stønad 3, ligger på samme sak og skal hentes
    periodeListe3.add(
        OpprettStonadPeriodeRequestDto(periodeFom = LocalDate.now(), periodeTil = LocalDate.now().plusDays(30), vedtakId = 3,
            gyldigFra = LocalDateTime.now(), gyldigTil = null, periodeGjortUgyldigAvVedtakId = null, belop = BigDecimal.valueOf(4477.03), valutakode = "NOK",
            resultatkode = "Alles gut"))
    val opprettStonadRequest3 = OpprettStonadRequestDto(StonadType.FORSKUDD, "SAK-001", "Skyldner001","Kravhaver001",
        "MottakerId001", "2024", Innkreving.JA, "R153961", periodeListe3)
    stonadService.opprettStonad(opprettStonadRequest3)


    val funnedeStonaderListe = stonadService.hentStonaderForSakId(opprettStonadRequest1.sakId)

    assertAll(
        Executable { assertThat(funnedeStonaderListe).size().isEqualTo(2) },
        Executable { assertThat(funnedeStonaderListe[0].type).isEqualTo(StonadType.BIDRAG) },
        Executable { assertThat(funnedeStonaderListe[0].sakId).isEqualTo("SAK-001") },
        Executable { assertThat(funnedeStonaderListe[0].skyldnerId).isEqualTo("Skyldner001") },

        Executable { assertThat(funnedeStonaderListe[0].periodeListe.size).isEqualTo(2) },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periodeFom).isEqualTo(LocalDate.now()) },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periodeTil).isEqualTo(LocalDate.now().plusDays(30)) },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },

        Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periodeFom).isEqualTo(LocalDate.now()) },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periodeTil).isEqualTo(LocalDate.now().plusDays(30)) },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periodeGjortUgyldigAvVedtakId).isNull() },
        Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(100.01)) },

        Executable { assertThat(funnedeStonaderListe[1].type).isEqualTo(StonadType.FORSKUDD) },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe.size).isEqualTo(1) },

        Executable { assertThat(funnedeStonaderListe[1].sakId).isEqualTo("SAK-001") },
        Executable { assertThat(funnedeStonaderListe[1].skyldnerId).isEqualTo("Skyldner001") },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe.size).isEqualTo(1) },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periodeFom).isEqualTo(LocalDate.now()) },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periodeTil).isEqualTo(LocalDate.now().plusDays(30)) },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periodeGjortUgyldigAvVedtakId).isNull() },
        Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(4477.03)) },


    )
  }


}