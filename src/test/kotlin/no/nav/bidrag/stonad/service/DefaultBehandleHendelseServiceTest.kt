package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Sporingsdata
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
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
@ActiveProfiles(BidragStonadTest.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
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

    val periodeliste = mutableListOf<Periode>()
    periodeliste.add(
      Periode(LocalDate.parse("2021-06-01"),
      LocalDate.parse("2021-07-01"), BigDecimal.valueOf(17.01), "NOK", "Hunky Dory", "referanse1")
    )

    val stonadsendringListe = mutableListOf<Stonadsendring>()
    stonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA,  true, periodeliste)
    )

    val nyHendelse = VedtakHendelse(VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1",  null, null, "R153961",
      LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
    )

    behandleHendelseService.behandleHendelse(nyHendelse)

    val nyStonadOpprettet = stonadService.hentStonad(HentStonadRequest(
      nyHendelse.stonadsendringListe!![0].type, nyHendelse.stonadsendringListe!![0].sakId,
      nyHendelse.stonadsendringListe!![0].skyldnerId, nyHendelse.stonadsendringListe!![0].kravhaverId))

    assertAll(
      Executable { Assertions.assertThat(nyStonadOpprettet!!).isNotNull() },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.type).isEqualTo(StonadType.BIDRAG) },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.sakId).isEqualTo("SAK-001") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.skyldnerId).isEqualTo("Skyldner1") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.kravhaverId).isEqualTo("Kravhaver1") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.mottakerId).isEqualTo("Mottaker1") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.opprettetAv).isEqualTo("R153961") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.indeksreguleringAar).isEqualTo("2024") },
      Executable { Assertions.assertThat(nyStonadOpprettet!!.innkreving).isEqualTo(Innkreving.JA) },
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
  fun `skal ikke opprette ny stonad fra Hendelse når endring == false`() {
    // Oppretter ny hendelse

    val periodeliste = mutableListOf<Periode>()
    periodeliste.add(
      Periode(LocalDate.parse("2021-06-01"),
        LocalDate.parse("2021-07-01"), BigDecimal.valueOf(17.01), "NOK", "Hunky Dory", "referanse1")
    )

    val stonadsendringListe = mutableListOf<Stonadsendring>()
    stonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA,  false, periodeliste)
    )

    val nyHendelse = VedtakHendelse(VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1",  null, null, "R153961",
      LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
    )

    behandleHendelseService.behandleHendelse(nyHendelse)

    val nyStonadOpprettet = stonadService.hentStonad(HentStonadRequest(
      nyHendelse.stonadsendringListe!![0].type, nyHendelse.stonadsendringListe!![0].sakId,
      nyHendelse.stonadsendringListe!![0].skyldnerId, nyHendelse.stonadsendringListe!![0].kravhaverId))

    assertAll(
      Executable { Assertions.assertThat(nyStonadOpprettet).isNull() }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Tester at perioder som er endret i nytt vedtak blir satt til ugyldig og erstattet av nye perioder
  fun `skal oppdatere eksisterende stønad med like fra- og til-datoer og ulike beløp, tester også på gyldigFra og gyldigTil`() {
    // Oppretter ny hendelse som etterpå skal oppdateres
    val originalPeriodeliste = mutableListOf<Periode>()
    originalPeriodeliste.add(Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"),
      BigDecimal.valueOf(17.01), "NOK", "Hunky Dory", "referanse1"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01"),
      BigDecimal.valueOf(17.02), "NOK", "Hunky Dory", "referanse2"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01"),
      BigDecimal.valueOf(17.03), "NOK", "Hunky Dory", "referanse3"))

    val originalStonadsendringListe = mutableListOf<Stonadsendring>()
    originalStonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
    )

    val originalHendelse = VedtakHendelse(kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 1,
        vedtakTidspunkt = LocalDateTime.parse("2020-10-17T10:12:14.169121000"), enhetId = "enhetId1",
        eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
        stonadsendringListe = originalStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata(""))

    behandleHendelseService.behandleHendelse(originalHendelse)
    val originalStonad = stonadService.hentStonad(HentStonadRequest(
      originalHendelse.stonadsendringListe!![0].type, originalHendelse.stonadsendringListe!![0].sakId,
      originalHendelse.stonadsendringListe!![0].skyldnerId, originalHendelse.stonadsendringListe!![0].kravhaverId))

    // Oppretter hendelse for nytt vedtak på samme stønad, stønaden over skal da oppdateres. Det er kun midterste periode her som er endret og skal oppdateres
    val periodeliste = mutableListOf<Periode>()
    periodeliste.add(Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"),
      BigDecimal.valueOf(17.01), "NOK", "Hunky Dory", "referanse1"))
    periodeliste.add(Periode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01"),
      BigDecimal.valueOf(100.02), "NOK", "Hunky Dory", "referanse2"))
    periodeliste.add(Periode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01"),
      BigDecimal.valueOf(17.03), "NOK", "Hunky Dory", "referanse3"))

    val stonadsendringListe = mutableListOf<Stonadsendring>()
    stonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "Sak1","Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
    )

    val hendelse = VedtakHendelse(kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 2,
        vedtakTidspunkt = LocalDateTime.parse("2020-10-20T20:12:14.246785000"), enhetId = "enhetId1",
        eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
        stonadsendringListe = stonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata(""))

    behandleHendelseService.behandleHendelse(hendelse)
    val oppdatertStonad = stonadService.hentStonad(HentStonadRequest(
      hendelse.stonadsendringListe!![0].type, hendelse.stonadsendringListe!![0].sakId,
      hendelse.stonadsendringListe!![0].skyldnerId, hendelse.stonadsendringListe!![0].kravhaverId))

    val allePerioderInkludertUgyldiggjorte = persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(oppdatertStonad!!.stonadId)

    assertAll(
      Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
      Executable { Assertions.assertThat(originalStonad!!.periodeListe.size).isEqualTo(3) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-02-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].valutakode).isEqualTo("NOK") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].resultatkode).isEqualTo("Hunky Dory") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-02-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].belop).isEqualTo(BigDecimal.valueOf(100.02)) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].valutakode).isEqualTo("NOK") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].resultatkode).isEqualTo("Hunky Dory") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2021-04-01")) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].belop).isEqualTo(BigDecimal.valueOf(17.03)) },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].valutakode).isEqualTo("NOK") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].resultatkode).isEqualTo("Hunky Dory") },
      Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

      Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte.size).isEqualTo(6) },
      Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-17T10:12:14.169121000")) },
      Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
      Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },

    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Tester at mottakerId blir oppdatert på eksisterende stønad
  fun `skal oppdatere mottakerId på eksisterende stønad`() {
    // Oppretter ny hendelse som etterpå skal oppdateres

    val originalStonadsendringListe = mutableListOf<Stonadsendring>()
    originalStonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, emptyList())
    )

    val originalHendelse = VedtakHendelse(VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1",  null, null,
      "R153961", LocalDateTime.now(), originalStonadsendringListe, emptyList(), Sporingsdata(""))

    behandleHendelseService.behandleHendelse(originalHendelse)
    val originalStonad = stonadService.hentStonad(HentStonadRequest(
      originalHendelse.stonadsendringListe!![0].type, originalHendelse.stonadsendringListe!![0].sakId,
      originalHendelse.stonadsendringListe!![0].skyldnerId, originalHendelse.stonadsendringListe!![0].kravhaverId))

    val stonadsendringListe = mutableListOf<Stonadsendring>()
    stonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "sak1","Skyldner1", "Kravhaver1", "Mottaker2", "2024", Innkreving.JA, true, emptyList())
    )

    val hendelse = VedtakHendelse(VedtakKilde.MANUELT, VedtakType.ENDRING_MOTTAKER, 1, LocalDateTime.now(), "enhetId1",  null, null,
      "R153961", LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata(""))

    behandleHendelseService.behandleHendelse(hendelse)
    val oppdatertStonad = stonadService.hentStonad(HentStonadRequest(
      hendelse.stonadsendringListe!![0].type, hendelse.stonadsendringListe!![0].sakId,
      hendelse.stonadsendringListe!![0].skyldnerId, hendelse.stonadsendringListe!![0].kravhaverId))

    assertAll(
      Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
      Executable { Assertions.assertThat(originalStonad!!.mottakerId).isEqualTo("Mottaker1") },
      Executable { Assertions.assertThat(oppdatertStonad!!.mottakerId).isEqualTo("Mottaker2") }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  // Tester at perioder på mottatt hendelse blir sortert etter fomdato og at periodeTil på lagret periode blir satt til lik
  // neste fomDato hvis mottatt periodeTil = null og det ikke er siste periode
  fun `test sortering av perioder på hendelse og justering av periodeTil på lagret periode hvis den er null`() {
    // Oppretter ny hendelse som etterpå skal oppdateres
    val originalPeriodeliste = mutableListOf<Periode>()
    originalPeriodeliste.add(Periode(LocalDate.parse("2017-01-01"), null,
      BigDecimal.valueOf(17.01), "NOK", "Hunky Dory", "referanse1"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2014-02-01"), null,
      BigDecimal.valueOf(17.02), "NOK", "Hunky Dory", "referanse2"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2021-06-01"), null,
      BigDecimal.valueOf(17.03), "NOK", "Hunky Dory", "referanse3"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2021-03-01"), null,
      BigDecimal.valueOf(17.04), "NOK", "Hunky Dory", "referanse4"))
    originalPeriodeliste.add(Periode(LocalDate.parse("2010-03-01"), null,
      BigDecimal.valueOf(17.05), "NOK", "Hunky Dory", "referanse5"))

    val stonadsendringListe = mutableListOf<Stonadsendring>()
    stonadsendringListe.add(
      Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
    )

    val hendelse = VedtakHendelse(VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1",  null, null,
      "R153961", LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata(""))

    behandleHendelseService.behandleHendelse(hendelse)
    val opprettetStonad = stonadService.hentStonad(HentStonadRequest(
      hendelse.stonadsendringListe!![0].type, hendelse.stonadsendringListe!![0].sakId,
      hendelse.stonadsendringListe!![0].skyldnerId, hendelse.stonadsendringListe!![0].kravhaverId))

    assertAll(
      Executable { Assertions.assertThat(opprettetStonad!!).isNotNull() },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe.size).isEqualTo(5) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2010-03-01")) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2014-02-01")) },

      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2014-02-01")) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[1].periodeTil).isEqualTo(LocalDate.parse("2017-01-01")) },

      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[2].periodeFom).isEqualTo(LocalDate.parse("2017-01-01")) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[2].periodeTil).isEqualTo(LocalDate.parse("2021-03-01")) },

      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[3].periodeFom).isEqualTo(LocalDate.parse("2021-03-01")) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[3].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },

      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[4].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[4].periodeTil).isNull() },
    )
  }


}