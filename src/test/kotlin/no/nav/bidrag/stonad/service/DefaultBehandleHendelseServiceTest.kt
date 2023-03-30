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
            Periode(
                LocalDate.parse("2021-06-01"),
                LocalDate.parse("2021-07-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
        )

        val nyHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null, "R153961",
            LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stonadService.hentStonad(
            HentStonadRequest(
                nyHendelse.stonadsendringListe!![0].type,
                nyHendelse.stonadsendringListe!![0].sakId,
                nyHendelse.stonadsendringListe!![0].skyldnerId,
                nyHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

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
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeFom)
                    .isEqualTo(LocalDate.parse("2021-06-01"))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeTil)
                    .isEqualTo(LocalDate.parse("2021-07-01"))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].belop)
                    .isEqualTo(BigDecimal.valueOf(17.01))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].valutakode)
                    .isEqualTo("NOK")
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].resultatkode)
                    .isEqualTo("Hunky Dory")
            }
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stonad fra Hendelse med ingen perioder`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
        )

        val nyHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null, "R153961",
            LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stonadService.hentStonad(
            HentStonadRequest(
                nyHendelse.stonadsendringListe!![0].type,
                nyHendelse.stonadsendringListe!![0].sakId,
                nyHendelse.stonadsendringListe!![0].skyldnerId,
                nyHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() }
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stonad fra Hendelse når endring = false eller Innkreving = nei`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                LocalDate.parse("2021-06-01"),
                LocalDate.parse("2021-07-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, false, periodeliste)
        )

        val nyHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null, "R153961",
            LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stonadService.hentStonad(
            HentStonadRequest(
                nyHendelse.stonadsendringListe!![0].type,
                nyHendelse.stonadsendringListe!![0].sakId,
                nyHendelse.stonadsendringListe!![0].skyldnerId,
                nyHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() }
        )
    }

    // Tester at perioder som er endret i nytt vedtak blir satt til ugyldig og erstattet av nye perioder
    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal oppdatere eksisterende stønad med like fra- og til-datoer og ulike beløp, tester også på gyldigFra og gyldigTil`() {
        // Oppretter ny hendelse som etterpå skal oppdateres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-02-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-02-01"),
                LocalDate.parse("2021-03-01"),
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-04-01"),
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3"
            )
        )

        val originalStonadsendringListe = mutableListOf<Stonadsendring>()
        originalStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val originalHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 1,
            vedtakTidspunkt = LocalDateTime.parse("2020-10-17T10:12:14.169121000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = originalStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(originalHendelse)
        val originalStonad = stonadService.hentStonad(
            HentStonadRequest(
                originalHendelse.stonadsendringListe!![0].type,
                originalHendelse.stonadsendringListe!![0].sakId,
                originalHendelse.stonadsendringListe!![0].skyldnerId,
                originalHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        // Oppretter hendelse for nytt vedtak på samme stønad, stønaden over skal da oppdateres. Det er kun midterste periode her som er endret og skal oppdateres
        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-02-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )
        periodeliste.add(
            Periode(
                LocalDate.parse("2021-02-01"),
                LocalDate.parse("2021-03-01"),
                BigDecimal.valueOf(100.02),
                "NOK",
                "Hunky Dory",
                "referanse2"
            )
        )
        periodeliste.add(
            Periode(
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-04-01"),
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3"
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
        )

        val hendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 2,
            vedtakTidspunkt = LocalDateTime.parse("2020-10-20T20:12:14.246785000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = stonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val oppdatertStonad = stonadService.hentStonad(
            HentStonadRequest(
                hendelse.stonadsendringListe!![0].type,
                hendelse.stonadsendringListe!![0].sakId,
                hendelse.stonadsendringListe!![0].skyldnerId,
                hendelse.stonadsendringListe!![0].kravhaverId
            )
        )

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
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) }

        )
    }

    // Tester at ugyldiggjorte perioder og nyopprettede perioder som følge av en splitt får satt riktig gyldigFra og gyldigTil
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på at gyldigFra og gyldigTil blir satt riktig ved splitt av perioder`() {
        // Oppretter ny hendelse som etterpå skal oppdateres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2022-01-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )

        val originalStonadsendringListe = mutableListOf<Stonadsendring>()
        originalStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val originalHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 1,
            vedtakTidspunkt = LocalDateTime.parse("2020-10-17T10:12:14.169121000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = originalStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for nytt vedtak på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal ugyldiggjøres og verdiene videreføres i to perioder,
        // én før den nye perioden og én etter.
        val foersteEndringPeriodeliste = mutableListOf<Periode>()

        foersteEndringPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-06-01"),
                LocalDate.parse("2021-08-01"),
                BigDecimal.valueOf(100.01),
                "NOK",
                "Endring1",
                "referanse1"
            )
        )

        val foersteEndringStonadsendringListe = mutableListOf<Stonadsendring>()
        foersteEndringStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, foersteEndringPeriodeliste)
        )

        val foersteEndringHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 2,
            vedtakTidspunkt = LocalDateTime.parse("2020-10-20T20:12:14.246785000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = foersteEndringStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(foersteEndringHendelse)

        // Oppretter hendelse for nytt vedtak på samme stønad. Den siste av de nyopprettede splittperiode skal ugyldiggjøres, splttes på nytt, og verdiene videreføres i to perioder,
        // én før den nye perioden og én etter.
        val andreEndringPeriodeliste = mutableListOf<Periode>()

        andreEndringPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-10-01"),
                LocalDate.parse("2021-11-01"),
                BigDecimal.valueOf(200.02),
                "NOK",
                "Endring2",
                "referanse2"
            )
        )

        val andreEndringStonadsendringListe = mutableListOf<Stonadsendring>()
        andreEndringStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, andreEndringPeriodeliste)
        )

        val andreEndringHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 3,
            vedtakTidspunkt = LocalDateTime.parse("2020-10-30T01:22:17.246755000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = andreEndringStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(andreEndringHendelse)

        val oppdatertStonad = stonadService.hentStonad(
            HentStonadRequest(
                foersteEndringHendelse.stonadsendringListe!![0].type,
                foersteEndringHendelse.stonadsendringListe!![0].sakId,
                foersteEndringHendelse.stonadsendringListe!![0].skyldnerId,
                foersteEndringHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        val allePerioderInkludertUgyldiggjorte = persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(oppdatertStonad!!.stonadId)

        assertAll(
            Executable { Assertions.assertThat(oppdatertStonad!!.periodeListe.size).isEqualTo(5) },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].belop).isEqualTo(BigDecimal.valueOf(100.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].resultatkode).isEqualTo("Endring1") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].periodeFom).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].periodeTil).isEqualTo(LocalDate.parse("2021-10-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeFom).isEqualTo(LocalDate.parse("2021-10-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeTil).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].belop).isEqualTo(BigDecimal.valueOf(200.02)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].resultatkode).isEqualTo("Endring2") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].periodeFom).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].periodeFom).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].periodeGjortUgyldigAvVedtakId).isEqualTo(3) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) }

        )
    }

    // Tester at løpende stønad får satt sluttdato ved vedtak om opphør og at perioder lenger frem i tid ugyldiggjøres, tester også nytt vedtak om gjenopptak av stønad
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på periodisering ved opphør og eksisterende perioder frem i tid`() {
        // Oppretter ny hendelse som etterpå skal oppdateres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-07-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-07-01"),
                null,
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2"
            )
        )

        val originalStonadsendringListe = mutableListOf<Stonadsendring>()
        originalStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val originalHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 1,
            vedtakTidspunkt = LocalDateTime.parse("2020-12-17T10:12:14.169121000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = originalStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for vedtak om opphør på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal få satt periodeTil = periodeFra på opphørsperiode
        val opphoerPeriodeliste = mutableListOf<Periode>()

        opphoerPeriodeliste.add(Periode(fomDato = LocalDate.parse("2021-06-01"), tilDato = null, belop = null, valutakode = null, resultatkode = "Opphoer", referanse = null))

        val opphoerStonadsendringListe = mutableListOf<Stonadsendring>()
        opphoerStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, opphoerPeriodeliste)
        )

        val opphoerHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 2,
            vedtakTidspunkt = LocalDateTime.parse("2021-05-20T20:12:14.246785000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = opphoerStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(opphoerHendelse)

        val oppdatertStonadEtterOpphoer = stonadService.hentStonad(
            HentStonadRequest(
                opphoerHendelse.stonadsendringListe!![0].type,
                opphoerHendelse.stonadsendringListe!![0].sakId,
                opphoerHendelse.stonadsendringListe!![0].skyldnerId,
                opphoerHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        val allePerioderEtterOpphoer = persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(oppdatertStonadEtterOpphoer!!.stonadId)

        // Oppretter hendelse for nytt vedtak for å gjenoppta samme stønad.
        val gjenopptagelsePeriodeliste = mutableListOf<Periode>()

        gjenopptagelsePeriodeliste.add(
            Periode(
                LocalDate.parse("2022-02-01"),
                null,
                BigDecimal.valueOf(200.02),
                "NOK",
                "Endring2",
                "referanse2"
            )
        )

        val gjenopptagelseStonadsendringListe = mutableListOf<Stonadsendring>()
        gjenopptagelseStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, gjenopptagelsePeriodeliste)
        )

        val gjenopptagelseHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 3,
            vedtakTidspunkt = LocalDateTime.parse("2022-01-30T01:22:17.246755000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = gjenopptagelseStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(gjenopptagelseHendelse)

        val gjenopptattStonad = stonadService.hentStonad(
            HentStonadRequest(
                opphoerHendelse.stonadsendringListe!![0].type,
                opphoerHendelse.stonadsendringListe!![0].sakId,
                opphoerHendelse.stonadsendringListe!![0].skyldnerId,
                opphoerHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        val allePerioderEtterGjenopptagelse = persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(gjenopptattStonad!!.stonadId)

        assertAll(

            Executable { Assertions.assertThat(gjenopptattStonad!!.periodeListe.size).isEqualTo(2) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse.size).isEqualTo(4) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].gyldigFra).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].periodeFom).isEqualTo(LocalDate.parse("2022-02-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].periodeTil).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].belop).isEqualTo(BigDecimal.valueOf(200.02)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].resultatkode).isEqualTo("Endring2") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].gyldigFra).isEqualTo(LocalDateTime.parse("2022-01-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].periodeFom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].periodeTil).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) }
        )
    }

    // Tester at løpende stønad med periodeTil = null får satt sluttdato ved vedtak om opphør
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på periodisering ved opphør`() {
        // Oppretter ny hendelse som etterpå skal opphøres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-07-01"),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-07-01"),
                null,
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2"
            )
        )

        val originalStonadsendringListe = mutableListOf<Stonadsendring>()
        originalStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val originalHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 1,
            vedtakTidspunkt = LocalDateTime.parse("2020-12-17T10:12:14.169121000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = originalStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for vedtak om opphør på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal få satt periodeTil = periodeFra på opphørsperiode
        val opphoerPeriodeliste = mutableListOf<Periode>()

        opphoerPeriodeliste.add(Periode(fomDato = LocalDate.parse("2021-11-01"), tilDato = null, belop = null, valutakode = null, resultatkode = "Opphoer", referanse = null))

        val opphoerStonadsendringListe = mutableListOf<Stonadsendring>()
        opphoerStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, opphoerPeriodeliste)
        )

        val opphoerHendelse = VedtakHendelse(
            kilde = VedtakKilde.MANUELT, type = VedtakType.ALDERSJUSTERING, id = 2,
            vedtakTidspunkt = LocalDateTime.parse("2021-05-20T20:12:14.246785000"), enhetId = "enhetId1",
            eksternReferanse = null, utsattTilDato = null, opprettetAv = "R153961", opprettetTidspunkt = LocalDateTime.now(),
            stonadsendringListe = opphoerStonadsendringListe, engangsbelopListe = emptyList(), sporingsdata = Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(opphoerHendelse)

        val oppdatertStonadEtterOpphoer = stonadService.hentStonad(
            HentStonadRequest(
                opphoerHendelse.stonadsendringListe!![0].type,
                opphoerHendelse.stonadsendringListe!![0].sakId,
                opphoerHendelse.stonadsendringListe!![0].skyldnerId,
                opphoerHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        val allePerioderEtterOpphoer = persistenceService.hentPerioderForStonadInkludertUgyldiggjorte(oppdatertStonadEtterOpphoer!!.stonadId)

        assertAll(

            Executable { Assertions.assertThat(oppdatertStonadEtterOpphoer!!.periodeListe.size).isEqualTo(2) },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer.size).isEqualTo(3) },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].periodeFom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].belop).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].periodeFom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].periodeTil).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].periodeGjortUgyldigAvVedtakId).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].gyldigFra).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].periodeFom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].periodeTil).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].periodeGjortUgyldigAvVedtakId).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) }

        )
    }

    // Tester at mottakerId blir oppdatert på eksisterende stønad
    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal oppdatere mottakerId på eksisterende stønad`() {
        // Oppretter ny hendelse som etterpå skal oppdateres

        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-01-01"),
                null,
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )

        val originalStonadsendringListe = mutableListOf<Stonadsendring>()
        originalStonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val originalHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null,
            "R153961", LocalDateTime.now(), originalStonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(originalHendelse)
        val originalStonad = stonadService.hentStonad(
            HentStonadRequest(
                originalHendelse.stonadsendringListe!![0].type,
                originalHendelse.stonadsendringListe!![0].sakId,
                originalHendelse.stonadsendringListe!![0].skyldnerId,
                originalHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker2", "2024", Innkreving.JA, true, emptyList())
        )

        val hendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ENDRING_MOTTAKER, 1, LocalDateTime.now(), "enhetId1", null, null,
            "R153961", LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val oppdatertStonad = stonadService.hentStonad(
            HentStonadRequest(
                hendelse.stonadsendringListe!![0].type,
                hendelse.stonadsendringListe!![0].sakId,
                hendelse.stonadsendringListe!![0].skyldnerId,
                hendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
            Executable { Assertions.assertThat(originalStonad!!.mottakerId).isEqualTo("Mottaker1") },
            Executable { Assertions.assertThat(oppdatertStonad!!.mottakerId).isEqualTo("Mottaker2") }
        )
    }

    // Tester at mottakerId blir oppdatert på eksisterende stønad
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på at forsøk på å oppdatere mottakerId på ikke-eksisterende stønad ikke forårsaker exceptions eller opprettelse av stønad`() {
        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "sak1", "Skyldner1", "Kravhaver1", "Mottaker2", "2024", Innkreving.JA, true, emptyList())
        )

        val hendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ENDRING_MOTTAKER, 1, LocalDateTime.now(), "enhetId1", null, null,
            "R153961", LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(hendelse)

        val stonad = stonadService.hentStonad(
            HentStonadRequest(
                hendelse.stonadsendringListe!![0].type,
                hendelse.stonadsendringListe!![0].sakId,
                hendelse.stonadsendringListe!![0].skyldnerId,
                hendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(stonad).isNull() }
        )
    }

    // Tester at perioder på mottatt hendelse blir sortert etter fomdato og at periodeTil på lagret periode blir satt til lik
    // neste fomDato hvis mottatt periodeTil = null og det ikke er siste periode
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test sortering av perioder på hendelse og justering av periodeTil på lagret periode hvis den er null`() {
        // Oppretter ny hendelse som etterpå skal oppdateres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2017-01-01"),
                null,
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2014-02-01"),
                null,
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-06-01"),
                null,
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2021-03-01"),
                null,
                BigDecimal.valueOf(17.04),
                "NOK",
                "Hunky Dory",
                "referanse4"
            )
        )
        originalPeriodeliste.add(
            Periode(
                LocalDate.parse("2010-03-01"),
                null,
                BigDecimal.valueOf(17.05),
                "NOK",
                "Hunky Dory",
                "referanse5"
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "Sak1", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, originalPeriodeliste)
        )

        val hendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null,
            "R153961", LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val opprettetStonad = stonadService.hentStonad(
            HentStonadRequest(
                hendelse.stonadsendringListe!![0].type,
                hendelse.stonadsendringListe!![0].sakId,
                hendelse.stonadsendringListe!![0].skyldnerId,
                hendelse.stonadsendringListe!![0].kravhaverId
            )
        )

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
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[4].periodeTil).isNull() }
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stonad fra Hendelse når beløp = null på alle perioder`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                LocalDate.parse("2021-06-01"),
                LocalDate.parse("2021-07-01"),
                null,
                "NOK",
                "AHI",
                "referanse1"
            )
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
        )

        val nyHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null, "R153961",
            LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stonadService.hentStonad(
            HentStonadRequest(
                nyHendelse.stonadsendringListe!![0].type,
                nyHendelse.stonadsendringListe!![0].sakId,
                nyHendelse.stonadsendringListe!![0].skyldnerId,
                nyHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() }
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `sjekk at justering av periodeTil bruker fomdato for neste periode selv om neste periode har beløp = null`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(LocalDate.parse("2021-06-01"), null, BigDecimal.valueOf(17.01), "NOK", "Alles gut", null)
        )
        periodeliste.add(
            Periode(LocalDate.parse("2021-09-01"), null, null, "NOK", "AHI", null)
        )
        periodeliste.add(
            Periode(LocalDate.parse("2021-12-01"), null, BigDecimal.valueOf(17.02), "NOK", "Alles gut", null)
        )

        val stonadsendringListe = mutableListOf<Stonadsendring>()
        stonadsendringListe.add(
            Stonadsendring(StonadType.BIDRAG, "SAK-001", "Skyldner1", "Kravhaver1", "Mottaker1", "2024", Innkreving.JA, true, periodeliste)
        )

        val nyHendelse = VedtakHendelse(
            VedtakKilde.MANUELT, VedtakType.ALDERSJUSTERING, 1, LocalDateTime.now(), "enhetId1", null, null, "R153961",
            LocalDateTime.now(), stonadsendringListe, emptyList(), Sporingsdata("")
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stonadService.hentStonad(
            HentStonadRequest(
                nyHendelse.stonadsendringListe!![0].type,
                nyHendelse.stonadsendringListe!![0].sakId,
                nyHendelse.stonadsendringListe!![0].skyldnerId,
                nyHendelse.stonadsendringListe!![0].kravhaverId
            )
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe.size).isEqualTo(2) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeFom).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-09-01")) },

            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[1].periodeFom).isEqualTo(LocalDate.parse("2021-12-01")) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[1].periodeTil).isNull() }

        )
    }
}
