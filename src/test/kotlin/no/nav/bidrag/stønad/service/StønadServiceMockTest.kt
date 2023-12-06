package no.nav.bidrag.stønad.service

import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.stønad.TestUtil.Companion.byggStonadRequest
import no.nav.bidrag.stønad.bo.PeriodeBo
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@DisplayName("stønadServiceMockTest")
@ExtendWith(MockitoExtension::class)
class StønadServiceMockTest {
    @InjectMocks
    private lateinit var stønadService: StønadService

    @Mock
    private lateinit var persistenceServiceMock: PersistenceService

    @Captor
    private lateinit var opprettStonadRequestDto: ArgumentCaptor<OpprettStønadRequestDto>

    @Captor
    private lateinit var periodeBoCaptor: ArgumentCaptor<PeriodeBo>

    @Test
    fun `skal opprette ny komplett stønad`() {
        Mockito.`when`(persistenceServiceMock.opprettStønad(MockitoHelper.capture(opprettStonadRequestDto)))
            .thenReturn(1)
        doNothing().`when`(persistenceServiceMock).opprettPeriode(MockitoHelper.capture(periodeBoCaptor), eq(1))

        val nyStonadOpprettetStonadId = stønadService.opprettStonad(byggStonadRequest())

        val stønadDto = opprettStonadRequestDto.value
        val periodeDtoListe = periodeBoCaptor.allValues

        Mockito.verify(persistenceServiceMock, Mockito.times(1))
            .opprettStønad(MockitoHelper.any(OpprettStønadRequestDto::class.java))

/*    Mockito.verify(persistenceServiceMock, Mockito.times(2))
      .opprettNyPeriode(MockitoHelper.any(PeriodeBo::class.java), nyStonadOpprettetStonadId)*/

        assertAll(
            Executable { assertThat(nyStonadOpprettetStonadId).isNotNull() },
            // Sjekk stønadDto
            Executable { assertThat(stønadDto).isNotNull() },
            Executable { assertThat(stønadDto.type).isEqualTo(Stønadstype.BIDRAG) },
            Executable { assertThat(stønadDto.sak).isEqualTo(Saksnummer("SAK-001")) },
            Executable { assertThat(stønadDto.skyldner).isEqualTo(Personident("01018011111")) },
            Executable { assertThat(stønadDto.kravhaver).isEqualTo(Personident("01010511111")) },
            Executable { assertThat(stønadDto.mottaker).isEqualTo(Personident("01018211111")) },
            Executable { assertThat(stønadDto.opprettetAv).isEqualTo("X123456") },
            Executable { assertThat(stønadDto.førsteIndeksreguleringsår).isEqualTo(2024) },
            Executable { assertThat(stønadDto.innkreving).isEqualTo(Innkrevingstype.MED_INNKREVING) },
            // Sjekk PeriodeDto
            Executable { assertThat(periodeDtoListe).isNotNull() },
            Executable { assertThat(periodeDtoListe.size).isEqualTo(2) },
            Executable { assertThat(periodeDtoListe[0].periode.fom).isEqualTo(YearMonth.parse("2019-01")) },
            Executable { assertThat(periodeDtoListe[0].periode.til).isEqualTo(YearMonth.parse("2019-07")) },
            Executable { assertThat(periodeDtoListe[0].vedtaksid).isEqualTo(321) },
            Executable { assertThat(periodeDtoListe[0].beløp).isEqualTo(BigDecimal.valueOf(3490)) },
            Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo("NOK") },
            Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },
            Executable { assertThat(periodeDtoListe[1].periode.fom).isEqualTo(YearMonth.parse("2019-07")) },
            Executable { assertThat(periodeDtoListe[1].periode.til).isEqualTo(YearMonth.parse("2020-01")) },
            Executable { assertThat(periodeDtoListe[1].vedtaksid).isEqualTo(323) },
            Executable { assertThat(periodeDtoListe[1].beløp).isEqualTo(BigDecimal.valueOf(3520)) },
            Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo("NOK") },
            Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },
        )
    }

    object MockitoHelper {
        // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
        fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

        fun <T> any(type: Class<T>): T = Mockito.any(type)
    }
}
