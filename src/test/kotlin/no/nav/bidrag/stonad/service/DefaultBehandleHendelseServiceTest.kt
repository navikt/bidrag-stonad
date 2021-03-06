package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelsePeriode
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName

import org.junit.jupiter.api.function.Executable
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("DefaultBehandleHendelseServiceTest")
@ActiveProfiles(BidragStonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DefaultBehandleHendelseServiceTest {

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var behandleHendelseService: DefaultBehandleHendelseService

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
  fun `skal opprette ny stonad fra Hendelse`() {
    // Oppretter ny hendelse
    val periodeliste = mutableListOf<VedtakHendelsePeriode>()
    periodeliste.add(
      VedtakHendelsePeriode(LocalDate.parse("2021-06-01"),
      LocalDate.parse("2021-07-01"), BigDecimal.valueOf(17.01), "NOK", "Hunky Dory")
    )

    val nyHendelse = VedtakHendelse(1, VedtakType.MANUELT,StonadType.BIDRAG, "SAK-001", "12345",
      "54321", "24680", "R153961",
    LocalDateTime.now(), periodeliste)

    behandleHendelseService.behandleHendelse(nyHendelse)

    val nyStonadOpprettet = stonadService.hentStonad(nyHendelse.stonadType.toString(), nyHendelse.skyldnerId, nyHendelse.kravhaverId)

    assertAll(
      Executable { Assertions.assertThat(nyStonadOpprettet!!).isNotNull() },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.stonadType).isEqualTo(StonadType.BIDRAG) },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.sakId).isEqualTo("SAK-001") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.skyldnerId).isEqualTo("12345") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.kravhaverId).isEqualTo("54321") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.mottakerId).isEqualTo("24680") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.opprettetAv).isEqualTo("R153961") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeFom)
        .isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeTil)
        .isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].belop)
        .isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].valutakode)
        .isEqualTo("NOK") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].resultatkode)
        .isEqualTo("Hunky Dory") }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Tester at perioder som er endret i nytt vedtak blir satt til ugyldig og erstattet av nye perioder
  fun `skal oppdatere eksisterende st??nad med like fra- og til-datoer og ulike bel??p`() {
    // Oppretter ny hendelse som etterp?? skal oppdateres
    val originalPeriodeliste = mutableListOf<VedtakHendelsePeriode>()
    originalPeriodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"),
      BigDecimal.valueOf(17.01), "NOK", "Hunky Dory"))
    originalPeriodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01"),
      BigDecimal.valueOf(17.02), "NOK", "Hunky Dory"))
    originalPeriodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01"),
      BigDecimal.valueOf(17.03), "NOK", "Hunky Dory"))

    val originalHendelse = VedtakHendelse(1, VedtakType.MANUELT,StonadType.BIDRAG, "SAK-001", "Skyldner123",
      "Kravhaver123", "MottakerId123", "R153961",
      LocalDateTime.now(), originalPeriodeliste)

    behandleHendelseService.behandleHendelse(originalHendelse)
    val originalStonad = stonadService.hentStonad(originalHendelse.stonadType.toString(), originalHendelse.skyldnerId, originalHendelse.kravhaverId)

    // Oppretter hendelse for nytt vedtak p?? samme st??nad, st??naden over skal da oppdateres. Det er kun midterste periode her som er endret og skal oppdateres
    val periodeliste = mutableListOf<VedtakHendelsePeriode>()
    periodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"),
      BigDecimal.valueOf(17.01), "NOK", "Hunky Dory"))
    periodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01"),
      BigDecimal.valueOf(100.02), "NOK", "Hunky Dory"))
    periodeliste.add(VedtakHendelsePeriode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01"),
      BigDecimal.valueOf(17.03), "NOK", "Hunky Dory"))

    val hendelse = VedtakHendelse(2, VedtakType.MANUELT,StonadType.BIDRAG, "SAK-001", "Skyldner123",
      "Kravhaver123", "MottakerId123", "R153961",
      LocalDateTime.now(), periodeliste)

    behandleHendelseService.behandleHendelse(hendelse)
    val oppdatertStonad = stonadService.hentStonad(hendelse.stonadType.toString(), hendelse.skyldnerId, hendelse.kravhaverId)

    assertAll(
      Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
      Executable { Assertions.assertThat(originalStonad!!.periodeListe.size).isEqualTo(3) },
      Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-02-01")) },
      Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe[0].valutakode).isEqualTo("NOK") },
      Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe[0].resultatkode).isEqualTo("Hunky Dory") }

    )
  }
}