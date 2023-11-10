package no.nav.bidrag.stønad.service

import no.nav.bidrag.domene.enums.Beslutningstype
import no.nav.bidrag.domene.enums.Innkrevingstype
import no.nav.bidrag.domene.enums.Stønadstype
import no.nav.bidrag.domene.enums.Vedtakskilde
import no.nav.bidrag.domene.enums.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.streng.Enhetsnummer
import no.nav.bidrag.domene.streng.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.BidragStønadTest
import no.nav.bidrag.stønad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stønad.persistence.repository.StønadRepository
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Sporingsdata
import no.nav.bidrag.transport.behandling.vedtak.Stønadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
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
import java.time.YearMonth

@DisplayName("DefaultBehandleHendelseServiceTest")
@ActiveProfiles(BidragStønadTest.TEST_PROFILE)
@SpringBootTest(classes = [BidragStønadTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
internal class DefaultBehandleHendelseServiceTest {

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var stønadRepository: StønadRepository

    @Autowired
    private lateinit var behandleHendelseService: DefaultBehandleHendelseService

    @Autowired
    private lateinit var stønadService: StønadService

    @Autowired
    private lateinit var persistenceService: PersistenceService

    @BeforeEach
    fun `init`() {
        // Sletter alle forekomster
        periodeRepository.deleteAll()
        stønadRepository.deleteAll()
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette ny stønad fra Hendelse`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2021-07-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"),
                2024, Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, periodeliste,
            ),
        )

        val nyHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null,
            null, "R153961", null,
            LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stønadService.hentStønad(
            HentStønadRequest(
                nyHendelse.stønadsendringListe!![0].type,
                nyHendelse.stønadsendringListe!![0].sak,
                nyHendelse.stønadsendringListe!![0].skyldner,
                nyHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet!!).isNotNull() },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.type).isEqualTo(Stønadstype.BIDRAG) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.sak.toString()).isEqualTo(Saksnummer("SAK-001").toString()) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.skyldner.toString()).isEqualTo(Personident("Skyldner1").toString()) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.kravhaver.toString()).isEqualTo(Personident("Kravhaver1").toString()) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.mottaker.toString()).isEqualTo(Personident("Mottaker1").toString()) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.opprettetAv).isEqualTo("R153961") },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.førsteIndeksreguleringsår).isEqualTo(2024) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.innkreving).isEqualTo(Innkrevingstype.MED_INNKREVING) },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periode.fom)
                    .isEqualTo(YearMonth.parse("2021-06"))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periode.til)
                    .isEqualTo(YearMonth.parse("2021-07"))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].beløp)
                    .isEqualTo(BigDecimal.valueOf(17.01))
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].valutakode)
                    .isEqualTo("NOK")
            },
            Executable {
                Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].resultatkode)
                    .isEqualTo("Hunky Dory")
            },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stønad fra Hendelse med ingen perioder`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner1"), Personident("Kravhaver1"),
                Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, periodeliste,
            ),
        )

        val nyHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"),
            null, null, "R153961", null,
            LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stønadService.hentStønad(
            HentStønadRequest(
                type = nyHendelse.stønadsendringListe!![0].type,
                sak = nyHendelse.stønadsendringListe!![0].sak,
                skyldner = nyHendelse.stønadsendringListe!![0].skyldner,
                kravhaver = nyHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stønad fra Hendelse når beslutning er Stadfestelse eller Innkrevingstype = nei`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2021-07-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.STADFESTELSE, null, null, periodeliste,
            ),
        )

        val nyHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null, null, "R153961", null,
            LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stønadService.hentStønad(
            HentStønadRequest(
                nyHendelse.stønadsendringListe!![0].type,
                nyHendelse.stønadsendringListe!![0].sak,
                nyHendelse.stønadsendringListe!![0].skyldner,
                nyHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() },
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
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01")),
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3",
            ),
        )

        val originalStønadsendringListe = mutableListOf<Stønadsendring>()
        originalStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val originalHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 1,
            vedtakstidspunkt = LocalDateTime.parse("2020-10-17T10:12:14.169121000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = originalStønadsendringListe, engangsbeløpListe = emptyList(), behandlingsreferanseListe = emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(originalHendelse)
        val originalStonad = stønadService.hentStønad(
            HentStønadRequest(
                originalHendelse.stønadsendringListe!![0].type,
                originalHendelse.stønadsendringListe!![0].sak,
                originalHendelse.stønadsendringListe!![0].skyldner,
                originalHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        // Oppretter hendelse for nytt vedtak på samme stønad, stønaden over skal da oppdateres. Det er kun midterste periode her som er endret og skal oppdateres
        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01")),
                BigDecimal.valueOf(100.02),
                "NOK",
                "Hunky Dory",
                "referanse2",
            ),
        )
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3",
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, periodeliste,
            ),
        )

        val hendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 2,
            vedtakstidspunkt = LocalDateTime.parse("2020-10-20T20:12:14.246785000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = stønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val oppdatertStonad = stønadService.hentStønad(
            HentStønadRequest(
                hendelse.stønadsendringListe!![0].type,
                hendelse.stønadsendringListe!![0].sak,
                hendelse.stønadsendringListe!![0].skyldner,
                hendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val allePerioderInkludertUgyldiggjorte = persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(oppdatertStonad!!.stønadsid)

        assertAll(
            Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
            Executable { Assertions.assertThat(originalStonad!!.periodeListe.size).isEqualTo(3) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-02")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-02")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(100.02)) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[1].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785")) },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte.size).isEqualTo(6) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },

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
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2022-01-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )

        val originalStønadsendringListe = mutableListOf<Stønadsendring>()
        originalStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val originalHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 1,
            vedtakstidspunkt = LocalDateTime.parse("2020-10-17T10:12:14.169121000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = originalStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for nytt vedtak på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal ugyldiggjøres og verdiene videreføres i to perioder,
        // én før den nye perioden og én etter.
        val foersteEndringPeriodeliste = mutableListOf<Periode>()

        foersteEndringPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2021-08-01")),
                BigDecimal.valueOf(100.01),
                "NOK",
                "Endring1",
                "referanse1",
            ),
        )

        val foersteEndringStønadsendringListe = mutableListOf<Stønadsendring>()
        foersteEndringStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, foersteEndringPeriodeliste,
            ),
        )

        val førsteEndringHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 2,
            vedtakstidspunkt = LocalDateTime.parse("2020-10-20T20:12:14.246785000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = foersteEndringStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(førsteEndringHendelse)

        // Oppretter hendelse for nytt vedtak på samme stønad. Den siste av de nyopprettede splittperiode skal ugyldiggjøres, splttes på nytt, og verdiene videreføres i to perioder,
        // én før den nye perioden og én etter.
        val andreEndringPeriodeliste = mutableListOf<Periode>()

        andreEndringPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-10-01"), LocalDate.parse("2021-11-01")),
                BigDecimal.valueOf(200.02),
                "NOK",
                "Endring2",
                "referanse2",
            ),
        )

        val andreEndringStønadsendringListe = mutableListOf<Stønadsendring>()
        andreEndringStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, andreEndringPeriodeliste,
            ),
        )

        val andreEndringHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 3,
            vedtakstidspunkt = LocalDateTime.parse("2020-10-30T01:22:17.246755000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = andreEndringStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(andreEndringHendelse)

        val oppdatertStonad = stønadService.hentStønad(
            HentStønadRequest(
                førsteEndringHendelse.stønadsendringListe!![0].type,
                førsteEndringHendelse.stønadsendringListe!![0].sak,
                førsteEndringHendelse.stønadsendringListe!![0].skyldner,
                førsteEndringHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val allePerioderInkludertUgyldiggjorte = persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(oppdatertStonad!!.stønadsid)

        assertAll(
            Executable { Assertions.assertThat(oppdatertStonad.periodeListe.size).isEqualTo(5) },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].fom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].til).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].fom).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].til).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].beløp).isEqualTo(BigDecimal.valueOf(100.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].resultatkode).isEqualTo("Endring1") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].fom).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].til).isEqualTo(LocalDate.parse("2021-10-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[2].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].fom).isEqualTo(LocalDate.parse("2021-10-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].til).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].beløp).isEqualTo(BigDecimal.valueOf(200.02)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].resultatkode).isEqualTo("Endring2") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[3].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].fom).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].til).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[4].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].fom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].til).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[5].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },

            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].fom).isEqualTo(LocalDate.parse("2021-08-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].til).isEqualTo(LocalDate.parse("2022-01-01")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].periodeGjortUgyldigAvVedtaksid).isEqualTo(3) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].gyldigFra).isEqualTo(LocalDateTime.parse("2020-10-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderInkludertUgyldiggjorte[6].gyldigTil).isEqualTo(LocalDateTime.parse("2020-10-30T01:22:17.246755000")) },

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
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-07-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-07-01"), null),
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2",
            ),
        )

        val originalStønadsendringListe = mutableListOf<Stønadsendring>()
        originalStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val originalHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 1,
            vedtakstidspunkt = LocalDateTime.parse("2020-12-17T10:12:14.169121000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = originalStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for vedtak om opphør på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal få satt til = periodeFra på opphørsperiode
        val opphoerPeriodeliste = mutableListOf<Periode>()

        opphoerPeriodeliste.add(Periode(periode = ÅrMånedsperiode(LocalDate.parse("2021-06-01"), null), beløp = null, valutakode = null, resultatkode = "Opphoer", delytelseId = null))

        val opphoerStønadsendringListe = mutableListOf<Stønadsendring>()
        opphoerStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, opphoerPeriodeliste,
            ),
        )

        val opphørHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 2,
            vedtakstidspunkt = LocalDateTime.parse("2021-05-20T20:12:14.246785000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = opphoerStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(opphørHendelse)

        val oppdatertStønadEtterOpphør = stønadService.hentStønad(
            HentStønadRequest(
                opphørHendelse.stønadsendringListe!![0].type,
                opphørHendelse.stønadsendringListe!![0].sak,
                opphørHendelse.stønadsendringListe!![0].skyldner,
                opphørHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val allePerioderEtterOpphoer = persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(oppdatertStønadEtterOpphør!!.stønadsid)

        // Oppretter hendelse for nytt vedtak for å gjenoppta samme stønad.
        val gjenopptagelsePeriodeliste = mutableListOf<Periode>()

        gjenopptagelsePeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2022-02-01"), null),
                BigDecimal.valueOf(200.02),
                "NOK",
                "Endring2",
                "referanse2",
            ),
        )

        val gjenopptagelseStønadsendringListe = mutableListOf<Stønadsendring>()
        gjenopptagelseStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, gjenopptagelsePeriodeliste,
            ),
        )

        val gjenopptagelseHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 3,
            vedtakstidspunkt = LocalDateTime.parse("2022-01-30T01:22:17.246755000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = gjenopptagelseStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(gjenopptagelseHendelse)

        val gjenopptattStonad = stønadService.hentStønad(
            HentStønadRequest(
                opphørHendelse.stønadsendringListe!![0].type,
                opphørHendelse.stønadsendringListe!![0].sak,
                opphørHendelse.stønadsendringListe!![0].skyldner,
                opphørHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val allePerioderEtterGjenopptagelse = persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(gjenopptattStonad!!.stønadsid)

        assertAll(

            Executable { Assertions.assertThat(gjenopptattStonad.periodeListe.size).isEqualTo(2) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse.size).isEqualTo(4) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].fom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].til).isEqualTo(LocalDate.parse("2021-06-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].gyldigFra).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].fom).isEqualTo(LocalDate.parse("2022-02-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].til).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].beløp).isEqualTo(BigDecimal.valueOf(200.02)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].resultatkode).isEqualTo("Endring2") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].gyldigFra).isEqualTo(LocalDateTime.parse("2022-01-30T01:22:17.246755000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].fom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].til).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[2].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },

            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].fom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].til).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterGjenopptagelse[3].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },
        )
    }

    // Tester at løpende stønad med til = null får satt sluttdato ved vedtak om opphør
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på periodisering ved opphør`() {
        // Oppretter ny hendelse som etterpå skal opphøres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-07-01")),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-07-01"), null),
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2",
            ),
        )

        val originalStønadsendringListe = mutableListOf<Stønadsendring>()
        originalStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val originalHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 1,
            vedtakstidspunkt = LocalDateTime.parse("2020-12-17T10:12:14.169121000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = originalStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(originalHendelse)

        // Oppretter hendelse for vedtak om opphør på samme stønad, stønaden over skal da oppdateres. Den originale perioden skal få satt til = periodeFra på opphørsperiode
        val opphoerPeriodeliste = mutableListOf<Periode>()

        opphoerPeriodeliste.add(Periode(ÅrMånedsperiode(LocalDate.parse("2021-11-01"), null), beløp = null, valutakode = null, resultatkode = "Opphoer", delytelseId = null))

        val opphoerStønadsendringListe = mutableListOf<Stønadsendring>()
        opphoerStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, opphoerPeriodeliste,
            ),
        )

        val opphoerHendelse = VedtakHendelse(
            kilde = Vedtakskilde.MANUELT, type = Vedtakstype.ALDERSJUSTERING, id = 2,
            vedtakstidspunkt = LocalDateTime.parse("2021-05-20T20:12:14.246785000"), enhetsnummer = Enhetsnummer("enhetsnummer1"),
            innkrevingUtsattTilDato = null, fastsattILand = null, opprettetAv = "R153961", opprettetAvNavn = "Sigge Saksbehandler", opprettetTidspunkt = LocalDateTime.now(),
            stønadsendringListe = opphoerStønadsendringListe, engangsbeløpListe = emptyList(), emptyList(), sporingsdata = Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(opphoerHendelse)

        val oppdatertStonadEtterOpphoer = stønadService.hentStønad(
            HentStønadRequest(
                opphoerHendelse.stønadsendringListe!![0].type,
                opphoerHendelse.stønadsendringListe!![0].sak,
                opphoerHendelse.stønadsendringListe!![0].skyldner,
                opphoerHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val allePerioderEtterOpphoer = persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(oppdatertStonadEtterOpphoer!!.stønadsid)

        assertAll(

            Executable { Assertions.assertThat(oppdatertStonadEtterOpphoer.periodeListe.size).isEqualTo(2) },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer.size).isEqualTo(3) },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].fom).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].til).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[0].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].fom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].til).isEqualTo(LocalDate.parse("2021-11-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].gyldigFra).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[1].gyldigTil).isNull() },

            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].fom).isEqualTo(LocalDate.parse("2021-07-01")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].til).isNull() },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].valutakode).isEqualTo("NOK") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].resultatkode).isEqualTo("Hunky Dory") },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].gyldigFra).isEqualTo(LocalDateTime.parse("2020-12-17T10:12:14.169121000")) },
            Executable { Assertions.assertThat(allePerioderEtterOpphoer[2].gyldigTil).isEqualTo(LocalDateTime.parse("2021-05-20T20:12:14.246785000")) },

        )
    }

    // Tester at mottaker blir oppdatert på eksisterende stønad
    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal oppdatere mottaker på eksisterende stønad`() {
        // Oppretter ny hendelse som etterpå skal oppdateres

        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), null),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )

        val originalStønadsendringListe = mutableListOf<Stønadsendring>()
        originalStønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val originalHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null,
            fastsattILand = null, "R153961", null, LocalDateTime.now(), originalStønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(originalHendelse)
        val originalStonad = stønadService.hentStønad(
            HentStønadRequest(
                originalHendelse.stønadsendringListe!![0].type,
                originalHendelse.stønadsendringListe!![0].sak,
                originalHendelse.stønadsendringListe!![0].skyldner,
                originalHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker2"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, emptyList(),
            ),
        )

        val hendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ENDRING_MOTTAKER, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null,
            fastsattILand = null, "R153961", null, LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val oppdatertStonad = stønadService.hentStønad(
            HentStønadRequest(
                hendelse.stønadsendringListe!![0].type,
                hendelse.stønadsendringListe!![0].sak,
                hendelse.stønadsendringListe!![0].skyldner,
                hendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(originalStonad!!).isNotNull() },
            Executable { Assertions.assertThat(originalStonad!!.mottaker.toString()).isEqualTo(Personident("Mottaker1").toString()) },
            Executable { Assertions.assertThat(oppdatertStonad!!.mottaker.toString()).isEqualTo(Personident("Mottaker2").toString()) },
        )
    }

    // Tester at mottaker blir oppdatert på eksisterende stønad
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test på at forsøk på å oppdatere mottaker på ikke-eksisterende stønad ikke forårsaker exceptions eller opprettelse av stønad`() {
        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker2"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, emptyList(),
            ),
        )

        val hendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ENDRING_MOTTAKER, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null,
            fastsattILand = null, "R153961", null, LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(hendelse)

        val stonad = stønadService.hentStønad(
            HentStønadRequest(
                hendelse.stønadsendringListe!![0].type,
                hendelse.stønadsendringListe!![0].sak,
                hendelse.stønadsendringListe!![0].skyldner,
                hendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(stonad).isNull() },
        )
    }

    // Tester at perioder på mottatt hendelse blir sortert etter fomdato og at til på lagret periode blir satt til lik
    // neste fomDato hvis mottatt til = null og det ikke er siste periode
    @Test
    @Suppress("NonAsciiCharacters")
    fun `test sortering av perioder på hendelse og justering av til på lagret periode hvis den er null`() {
        // Oppretter ny hendelse som etterpå skal oppdateres
        val originalPeriodeliste = mutableListOf<Periode>()
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2017-01-01"), null),
                BigDecimal.valueOf(17.01),
                "NOK",
                "Hunky Dory",
                "referanse1",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2014-02-01"), null),
                BigDecimal.valueOf(17.02),
                "NOK",
                "Hunky Dory",
                "referanse2",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), null),
                BigDecimal.valueOf(17.03),
                "NOK",
                "Hunky Dory",
                "referanse3",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), null),
                BigDecimal.valueOf(17.04),
                "NOK",
                "Hunky Dory",
                "referanse4",
            ),
        )
        originalPeriodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2010-03-01"), null),
                BigDecimal.valueOf(17.05),
                "NOK",
                "Hunky Dory",
                "referanse5",
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("Sak1"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, originalPeriodeliste,
            ),
        )

        val hendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null,
            fastsattILand = null, "R153961", null, LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(hendelse)
        val opprettetStonad = stønadService.hentStønad(
            HentStønadRequest(
                hendelse.stønadsendringListe!![0].type,
                hendelse.stønadsendringListe!![0].sak,
                hendelse.stønadsendringListe!![0].skyldner,
                hendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(opprettetStonad!!).isNotNull() },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe.size).isEqualTo(5) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2010-03")) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2014-02")) },

            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2014-02")) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2017-01")) },

            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2017-01")) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2021-03")) },

            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[3].periode.til).isEqualTo(YearMonth.parse("2021-06")) },

            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[4].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { Assertions.assertThat(opprettetStonad!!.periodeListe[4].periode.til).isNull() },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ikke opprette ny stønad fra Hendelse når beløp = null på alle perioder`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2021-07-01")),
                null,
                "NOK",
                "AHI",
                "referanse1",
            ),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, periodeliste,
            ),
        )

        val nyHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null, null, "R153961", null,
            LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stønadService.hentStønad(
            HentStønadRequest(
                nyHendelse.stønadsendringListe!![0].type,
                nyHendelse.stønadsendringListe!![0].sak,
                nyHendelse.stønadsendringListe!![0].skyldner,
                nyHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet).isNull() },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `sjekk at justering av til bruker fomdato for neste periode selv om neste periode har beløp = null`() {
        // Oppretter ny hendelse

        val periodeliste = mutableListOf<Periode>()
        periodeliste.add(
            Periode(ÅrMånedsperiode(LocalDate.parse("2021-06-01"), null), BigDecimal.valueOf(17.01), "NOK", "Alles gut", null),
        )
        periodeliste.add(
            Periode(ÅrMånedsperiode(LocalDate.parse("2021-09-01"), null), null, "NOK", "AHI", null),
        )
        periodeliste.add(
            Periode(ÅrMånedsperiode(LocalDate.parse("2021-12-01"), null), BigDecimal.valueOf(17.02), "NOK", "Alles gut", null),
        )

        val stønadsendringListe = mutableListOf<Stønadsendring>()
        stønadsendringListe.add(
            Stønadsendring(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner1"), Personident("Kravhaver1"), Personident("Mottaker1"), 2024,
                Innkrevingstype.MED_INNKREVING, Beslutningstype.ENDRING, null, null, periodeliste,
            ),
        )

        val nyHendelse = VedtakHendelse(
            Vedtakskilde.MANUELT, Vedtakstype.ALDERSJUSTERING, 1, LocalDateTime.now(), Enhetsnummer("enhetsnummer1"), null, null, "R153961", null,
            LocalDateTime.now(), stønadsendringListe, emptyList(), emptyList(), Sporingsdata(""),
        )

        behandleHendelseService.behandleHendelse(nyHendelse)

        val nyStonadOpprettet = stønadService.hentStønad(
            HentStønadRequest(
                nyHendelse.stønadsendringListe!![0].type,
                nyHendelse.stønadsendringListe!![0].sak,
                nyHendelse.stønadsendringListe!![0].skyldner,
                nyHendelse.stønadsendringListe!![0].kravhaver,
            ),
        )

        assertAll(
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe.size).isEqualTo(2) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-09")) },

            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-12")) },
            Executable { Assertions.assertThat(nyStonadOpprettet!!.periodeListe[1].periode.til).isNull() },

        )
    }
}
