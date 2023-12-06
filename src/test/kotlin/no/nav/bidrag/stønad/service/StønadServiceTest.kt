package no.nav.bidrag.stønad.service

import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.BidragStønadTest
import no.nav.bidrag.stønad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stønad.persistence.repository.StønadRepository
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto
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
import java.time.YearMonth

@DisplayName("stønadServiceTest")
@ActiveProfiles(BidragStønadTest.TEST_PROFILE)
@SpringBootTest(
    classes = [BidragStønadTest::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class StønadServiceTest {
    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var stønadRepository: StønadRepository

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
    fun `skal opprette ny stønad`() {
        // Oppretter ny stønad
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val opprettStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"),
                Personident("Kravhaver123"), Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        val nyStonadOpprettet = stønadService.opprettStonad(opprettStonadRequest)

        assertAll(
            Executable { assertThat(nyStonadOpprettet).isNotNull() },
        )
    }

    @Test
    // Returnerer stønad og alle perioder som ikke er markert som ugyldige
    @Suppress("NonAsciiCharacters")
    fun `skal finne alle gyldige perioder for en stønad`() {
        // Oppretter ny stønad
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = 1,
                beløp = BigDecimal.valueOf(17.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.02),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-04-01"), LocalDate.parse("2021-05-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val opprettStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"),
                Personident("Kravhaver123"), Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961",
                periodeListe,
            )

        stønadService.opprettStonad(opprettStønadRequest)

        val opprettetStonad =
            stønadService.hentStønad(
                HentStønadRequest(
                    opprettStønadRequest.type,
                    opprettStønadRequest.sak,
                    opprettStønadRequest.skyldner,
                    opprettStønadRequest.kravhaver,
                ),
            )

        assertAll(
            Executable { assertThat(opprettetStonad).isNotNull() },
            Executable { assertThat(opprettetStonad!!.periodeListe.size).isEqualTo(3) },
            Executable { assertThat(opprettetStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-02")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(opprettetStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.02)) },
            Executable { assertThat(opprettetStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(opprettetStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal finne alle perioder for en stønad, også ugyldiggjorte - Ugyldiggjorte kommer etter gyldige perioder`() {
        // Oppretter ny stønad
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-02-01"), LocalDate.parse("2021-03-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = 1,
                beløp = BigDecimal.valueOf(17.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-04-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.02),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-04-01"), LocalDate.parse("2021-05-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val opprettStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(opprettStønadRequest)

        val funnetStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                opprettStønadRequest.type.toString(),
                opprettStønadRequest.skyldner.verdi,
                opprettStønadRequest.kravhaver.verdi,
                opprettStønadRequest.sak.toString(),
            )

        assertAll(
            Executable { assertThat(funnetStonad).isNotNull() },
            Executable { assertThat(funnetStonad!!.periodeListe.size).isEqualTo(4) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-02")) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnetStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnetStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.02)) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnetStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { assertThat(funnetStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(funnetStonad!!.periodeListe[3].periode.til).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(funnetStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(1) },
            Executable { assertThat(funnetStonad!!.periodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal finne stønad fra sammensatt nøkkel`() {
        // Oppretter ny stønad

        val periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto(
                    ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 1,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(1),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            )

        val nyStønadOpprettetStønadsid =
            persistenceService.opprettStønad(
                OpprettStønadRequestDto(
                    Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"),
                    Personident(
                        "Kravhaver123",
                    ),
                    Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
                ),
            )

        val nyStonadOpprettet = persistenceService.hentStønadFraId(nyStønadOpprettetStønadsid)

        // Finner stønaden som akkurat ble opprettet
        val stønadFunnet =
            stønadService.hentStønad(
                HentStønadRequest(
                    Stønadstype.valueOf(nyStonadOpprettet!!.type),
                    Saksnummer(nyStonadOpprettet.sak),
                    Personident(nyStonadOpprettet.skyldner),
                    Personident(nyStonadOpprettet.kravhaver),
                ),
            )

        assertAll(
            Executable { assertThat(stønadFunnet).isNotNull() },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal finne stønad fra generert id`() {
        // Oppretter ny stonad
        val periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto(
                    ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 1,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(1),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            )

        val nyStønadOpprettetStønadsid =
            persistenceService.opprettStønad(
                OpprettStønadRequestDto(
                    Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"),
                    Personident("Kravhaver123"), Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961",
                    periodeListe,
                ),
            )

        // Finner stønaden som akkurat ble opprettet
        val stonadFunnet = stønadService.hentStonadFraId(nyStønadOpprettetStønadsid)

        assertAll(
            Executable { assertThat(stonadFunnet).isNotNull() },
        )
    }

    // endrer eksisterende stønad og ugyldiggjør perioder som har blitt endret i nytt vedtak
    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal endre eksisterende stønad`() {
        // Oppretter først stønaden som skal endres etterpå
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-03-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-07-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-07-01"), LocalDate.parse("2021-12-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val originalStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(originalStønadRequest)
        val originalStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                originalStønadRequest.type.toString(),
                originalStønadRequest.skyldner.verdi,
                originalStønadRequest.kravhaver.verdi,
                originalStønadRequest.sak.toString(),
            )

        // Oppretter så ny request som skal oppdatere eksisterende stønad
        val endretStonadPeriodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.01),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2021-08-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.02),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-08-01"), LocalDate.parse("2021-10-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.03),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )

        val endretStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", endretStonadPeriodeListe,
            )

        stønadService.endreStonad(originalStonad!!, endretStønadRequest, LocalDateTime.now())
        val endretStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                endretStønadRequest.type.toString(),
                endretStønadRequest.skyldner.verdi,
                endretStønadRequest.kravhaver.verdi,
                endretStønadRequest.sak.toString(),
            )

        assertAll(
            // Perioder sorteres på periodeGjortUgyldigAvVedtaksid så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtaksid kommer sist.
            Executable { assertThat(endretStonad).isNotNull() },
            Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(8) },
            // Første periode er før perioder for nytt vedtak, blir ikke endret
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Avkortet utgave av ugyldiggjort periode med til-dato lik fom-dato for nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(5000.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.til).isEqualTo(YearMonth.parse("2021-08")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(5000.02)) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[4].periode.fom).isEqualTo(YearMonth.parse("2021-08")) },
            Executable { assertThat(endretStonad!!.periodeListe[4].periode.til).isEqualTo(YearMonth.parse("2021-10")) },
            Executable { assertThat(endretStonad!!.periodeListe[4].beløp).isEqualTo(BigDecimal.valueOf(5000.03)) },
            Executable { assertThat(endretStonad!!.periodeListe[4].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Avkortet utgave av ugyldiggjort periode med fom-dato lik til-dato for nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[5].periode.fom).isEqualTo(YearMonth.parse("2021-10")) },
            Executable { assertThat(endretStonad!!.periodeListe[5].periode.til).isEqualTo(YearMonth.parse("2021-12")) },
            Executable { assertThat(endretStonad!!.periodeListe[5].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { assertThat(endretStonad!!.periodeListe[5].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[6].periode.fom).isEqualTo(YearMonth.parse("2021-03")) },
            Executable { assertThat(endretStonad!!.periodeListe[6].periode.til).isEqualTo(YearMonth.parse("2021-07")) },
            Executable { assertThat(endretStonad!!.periodeListe[6].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { assertThat(endretStonad!!.periodeListe[6].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[7].periode.fom).isEqualTo(YearMonth.parse("2021-07")) },
            Executable { assertThat(endretStonad!!.periodeListe[7].periode.til).isEqualTo(YearMonth.parse("2021-12")) },
            Executable { assertThat(endretStonad!!.periodeListe[7].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { assertThat(endretStonad!!.periodeListe[7].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
        )
    }

    // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
    @Test
    @Suppress("NonAsciiCharacters")
    fun `Test på splitt av perioder med vedtak med periode midt i eksisterende stønad`() {
        // Oppretter først stønaden som skal endres etterpå
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2022-01-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val originalStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(originalStønadRequest)
        val originalStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                originalStønadRequest.type.toString(),
                originalStønadRequest.skyldner.verdi,
                originalStønadRequest.kravhaver.verdi,
                originalStønadRequest.sak.toString(),
            )

        // Oppretter så ny request som skal oppdatere eksisterende stønad
        val endretStonadPeriodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.01),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )

        val endretStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", endretStonadPeriodeListe,
            )

        stønadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
        val endretStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                endretStonadRequest.type.toString(),
                endretStonadRequest.skyldner.verdi,
                endretStonadRequest.kravhaver.verdi,
                endretStonadRequest.sak.toString(),
            )

        assertAll(
            // Perioder sorteres på periodeGjortUgyldigAvVedtaksid så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtaksid kommer sist.
            Executable { assertThat(endretStonad).isNotNull() },
            Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(4) },
            // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
            // Første periode
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Siste periode fra eksisterende stønad
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2022-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.til).isEqualTo(YearMonth.parse("2022-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
        )
    }

    // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
    @Test
    @Suppress("NonAsciiCharacters")
    fun `Test med null i tildato på ny vedtaksperiode`() {
        // Oppretter først stønaden som skal endres etterpå
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2022-01-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val originalStønadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(originalStønadRequest)
        val originalStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                originalStønadRequest.type.toString(),
                originalStønadRequest.skyldner.verdi,
                originalStønadRequest.kravhaver.verdi,
                originalStønadRequest.sak.toString(),
            )

        // Oppretter så ny request som skal oppdatere eksisterende stønad
        val endretStonadPeriodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), til = null),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.01),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )

        val endretStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", endretStonadPeriodeListe,
            )

        stønadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
        val endretStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                endretStonadRequest.type.toString(),
                endretStonadRequest.skyldner.verdi,
                endretStonadRequest.kravhaver.verdi,
                endretStonadRequest.sak.toString(),
            )

        assertAll(
            // Perioder sorteres på periodeGjortUgyldigAvVedtaksid så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtaksid kommer sist.
            Executable { assertThat(endretStonad).isNotNull() },
            Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(3) },
            // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
            // Første periode
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.til).isNull() },
            Executable { assertThat(endretStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2022-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
        )
    }

    // Perioder i eksisterende stønad skal ugyldiggjøres og erstattes med nye perioder med like data og justerte datoer
    @Test
    @Suppress("NonAsciiCharacters")
    fun `Test med null i tildato på eksisterende stønadsperiode`() {
        // Oppretter først stønaden som skal endres etterpå
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), til = null),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val originalStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(originalStonadRequest)
        val originalStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                originalStonadRequest.type.toString(),
                originalStonadRequest.skyldner.verdi,
                originalStonadRequest.kravhaver.verdi,
                originalStonadRequest.sak.toString(),
            )

        // Oppretter så ny request som skal oppdatere eksisterende stønad
        val endretStonadPeriodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.01),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )

        val endretStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", endretStonadPeriodeListe,
            )

        stønadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
        val endretStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                endretStonadRequest.type.toString(),
                endretStonadRequest.skyldner.verdi,
                endretStonadRequest.kravhaver.verdi,
                endretStonadRequest.sak.toString(),
            )

        assertAll(
            // Perioder sorteres på periodeGjortUgyldigAvVedtaksid så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtaksid kommer sist.
            Executable { assertThat(endretStonad).isNotNull() },
            Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(4) },
            // Periode for eksisterende stønad ugyldigjøres og kopieres til to nye perioder, én for og én etter periode fra nytt vedtak.
            // Første periode
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Siste periode fra eksisterende stønad
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.til).isNull() },
            Executable { assertThat(endretStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.til).isNull() },
            Executable { assertThat(endretStonad!!.periodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
        )
    }

    // Alle perioder i eksisterende stønad som befinner seg innenfor fra- og tildato for nytt vedtak skal erstattes selv om det finnes
    // en identisk periode i det nye vedtaket.
    @Test
    @Suppress("NonAsciiCharacters")
    fun `Test med like perioder og endret beløp i én periode`() {
        // Oppretter først stønaden som skal endres etterpå
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-05-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), til = null),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val originalStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        stønadService.opprettStonad(originalStonadRequest)
        val originalStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                originalStonadRequest.type.toString(),
                originalStonadRequest.skyldner.verdi,
                originalStonadRequest.kravhaver.verdi,
                originalStonadRequest.sak.toString(),
            )

        // Oppretter så ny request som skal oppdatere eksisterende stønad
        val endretStonadPeriodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()

        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-05-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(5000.01),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )

        endretStonadPeriodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), til = null),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val endretStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", endretStonadPeriodeListe,
            )

        stønadService.endreStonad(originalStonad!!, endretStonadRequest, LocalDateTime.now())
        val endretStonad =
            stønadService.hentStønadInkludertUgyldiggjortePerioder(
                endretStonadRequest.type.toString(),
                endretStonadRequest.skyldner.verdi,
                endretStonadRequest.kravhaver.verdi,
                endretStonadRequest.sak.toString(),
            )

        assertAll(
            // Perioder sorteres på periodeGjortUgyldigAvVedtaksid så fom-dato. Perioder med null i periodeGjortUgyldigAvVedtaksid kommer sist.
            Executable { assertThat(endretStonad).isNotNull() },
            Executable { assertThat(endretStonad!!.periodeListe.size).isEqualTo(6) },
            // Alle perioder for eksisterende stønad ugyldigjøres selv om noen av periodene er identiske
            // Første periode
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[0].vedtaksid).isEqualTo(2) },
            Executable { assertThat(endretStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Periode fra nytt vedtak
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[1].vedtaksid).isEqualTo(2) },
            Executable { assertThat(endretStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Siste periode fra eksisterende stønad
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periode.til).isNull() },
            Executable { assertThat(endretStonad!!.periodeListe[2].vedtaksid).isEqualTo(2) },
            Executable { assertThat(endretStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { assertThat(endretStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isNull() },
            // Perioden overlapper med nytt vedtak, settes til ugyldig
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periode.til).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[3].vedtaksid).isEqualTo(1) },
            Executable { assertThat(endretStonad!!.periodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(endretStonad!!.periodeListe[3].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { assertThat(endretStonad!!.periodeListe[4].periode.fom).isEqualTo(YearMonth.parse("2021-05")) },
            Executable { assertThat(endretStonad!!.periodeListe[4].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[4].vedtaksid).isEqualTo(1) },
            Executable { assertThat(endretStonad!!.periodeListe[4].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { assertThat(endretStonad!!.periodeListe[4].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { assertThat(endretStonad!!.periodeListe[5].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(endretStonad!!.periodeListe[5].periode.til).isNull() },
            Executable { assertThat(endretStonad!!.periodeListe[5].vedtaksid).isEqualTo(1) },
            Executable { assertThat(endretStonad!!.periodeListe[5].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
            Executable { assertThat(endretStonad!!.periodeListe[5].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal finne historiske perioder for en stønad`() {
        // Oppretter ny stønad
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        // Legger først til periode som ikke skal returneres
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2022-01-01")),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.parse("2021-01-17T17:17:17.179121094"),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-04-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"),
                gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"),
                periodeGjortUgyldigAvVedtaksid = 2,
                beløp = BigDecimal.valueOf(17.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-04-01"), LocalDate.parse("2021-06-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"),
                gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"),
                periodeGjortUgyldigAvVedtaksid = 2,
                beløp = BigDecimal.valueOf(5000.02),
                valutakode = "NOK",
                resultatkode = "Ny periode lagt til",
            ),
        )
        periodeListe.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.parse("2021-06-01"), LocalDate.parse("2022-01-01")),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.parse("2020-10-17T10:12:14.169121094"),
                gyldigTil = LocalDateTime.parse("2021-01-17T17:17:17.179121094"),
                periodeGjortUgyldigAvVedtaksid = 2,
                beløp = BigDecimal.valueOf(17.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )

        val opprettStonadRequest =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner123"), Personident("Kravhaver123"),
                Personident("MottakerId123"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe,
            )

        val stønadsid = stønadService.opprettStonad(opprettStonadRequest)

        val funnetStonad =
            stønadService.hentStønadHistorisk(
                HentStønadHistoriskRequest(
                    opprettStonadRequest.type,
                    opprettStonadRequest.sak,
                    opprettStonadRequest.skyldner,
                    opprettStonadRequest.kravhaver,
                    LocalDateTime.parse("2020-12-31T23:00:00.169121094"),
                ),
            )

        assertAll(
            Executable { assertThat(funnetStonad).isNotNull() },
            Executable { assertThat(funnetStonad!!.periodeListe.size).isEqualTo(3) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2021-01")) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { assertThat(funnetStonad!!.periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.02)) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2021-04")) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { assertThat(funnetStonad!!.periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(5000.02)) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periode.fom).isEqualTo(YearMonth.parse("2021-06")) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periode.til).isEqualTo(YearMonth.parse("2022-01")) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].periodeGjortUgyldigAvVedtaksid).isEqualTo(2) },
            Executable { assertThat(funnetStonad!!.periodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(17.03)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal finne alle stønader for angitt sak`() {
        // Oppretter ny stønad
        val periodeListe1 = mutableListOf<OpprettStønadsperiodeRequestDto>()
        val periodeListe2 = mutableListOf<OpprettStønadsperiodeRequestDto>()
        val periodeListe3 = mutableListOf<OpprettStønadsperiodeRequestDto>()
        // Oppretter stønad 1
        periodeListe1.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.now(), LocalDate.now().plusMonths(1)),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(17.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        periodeListe1.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.now(), LocalDate.now().plusMonths(1)),
                vedtaksid = 1,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(100.01),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        val opprettStonadRequest1 =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-001"), Personident("Skyldner001"), Personident("Kravhaver001"),
                Personident("Mottaker001"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe1,
            )
        stønadService.opprettStonad(opprettStonadRequest1)

        // Oppretter stønad 2, ligger på en annen sak og skal ikke hentes
        periodeListe2.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.now(), LocalDate.now().plusDays(30)),
                vedtaksid = 2,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(998.02),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        val opprettStonadRequest2 =
            OpprettStønadRequestDto(
                Stønadstype.BIDRAG, Saksnummer("SAK-002"), Personident("Skyldner002"), Personident("Kravhaver002"),
                Personident("Mottaker002"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe2,
            )
        stønadService.opprettStonad(opprettStonadRequest2)

        // Oppretter stønad 3, ligger på samme sak og skal hentes
        periodeListe3.add(
            OpprettStønadsperiodeRequestDto(
                ÅrMånedsperiode(LocalDate.now(), LocalDate.now().plusDays(30)),
                vedtaksid = 3,
                gyldigFra = LocalDateTime.now(),
                gyldigTil = null,
                periodeGjortUgyldigAvVedtaksid = null,
                beløp = BigDecimal.valueOf(4477.03),
                valutakode = "NOK",
                resultatkode = "Alles gut",
            ),
        )
        val opprettStonadRequest3 =
            OpprettStønadRequestDto(
                Stønadstype.FORSKUDD, Saksnummer("SAK-001"), Personident("Skyldner001"), Personident("Kravhaver001"),
                Personident("Mottaker001"), 2024, Innkrevingstype.MED_INNKREVING, "R153961", periodeListe3,
            )
        stønadService.opprettStonad(opprettStonadRequest3)

        val funnedeStonaderListe = stønadService.hentStønaderForSak(opprettStonadRequest1.sak.toString())

        assertAll(
            Executable { assertThat(funnedeStonaderListe).size().isEqualTo(2) },
            Executable { assertThat(funnedeStonaderListe[0].type).isEqualTo(Stønadstype.BIDRAG) },
            Executable { assertThat(funnedeStonaderListe[0].sak.toString()).isEqualTo(Saksnummer("SAK-001").toString()) },
            Executable { assertThat(funnedeStonaderListe[0].skyldner.verdi).isEqualTo(Personident("Skyldner001").verdi) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe.size).isEqualTo(2) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periode.fom).isEqualTo(YearMonth.now()) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periode.til).isEqualTo(YearMonth.now().plusMonths(1)) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(17.01)) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periode.fom).isEqualTo(YearMonth.now()) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periode.til).isEqualTo(YearMonth.now().plusMonths(1)) },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnedeStonaderListe[0].periodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(100.01)) },
            Executable { assertThat(funnedeStonaderListe[1].type).isEqualTo(Stønadstype.FORSKUDD) },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe.size).isEqualTo(1) },
            Executable { assertThat(funnedeStonaderListe[1].sak.toString()).isEqualTo("SAK-001") },
            Executable { assertThat(funnedeStonaderListe[1].skyldner.verdi).isEqualTo(Personident("Skyldner001").verdi) },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe.size).isEqualTo(1) },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periode.fom).isEqualTo(YearMonth.now()) },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periode.til).isEqualTo(YearMonth.now().plusMonths(1)) },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].periodeGjortUgyldigAvVedtaksid).isNull() },
            Executable { assertThat(funnedeStonaderListe[1].periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(4477.03)) },
        )
    }
}
