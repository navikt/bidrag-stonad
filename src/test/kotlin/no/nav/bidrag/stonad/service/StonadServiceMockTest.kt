package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.TestUtil.Companion.byggStonadRequest
import no.nav.bidrag.stonad.bo.PeriodeBo
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
import java.time.LocalDate

@DisplayName("stonadServiceMockTest")
@ExtendWith(MockitoExtension::class)
class StonadServiceMockTest {

  @InjectMocks
  private lateinit var stonadService: StonadService

  @Mock
  private lateinit var persistenceServiceMock: PersistenceService

  @Captor
  private lateinit var opprettStonadRequestDto: ArgumentCaptor<OpprettStonadRequestDto>

  @Captor
  private lateinit var periodeBoCaptor: ArgumentCaptor<PeriodeBo>

  @Test
  fun `skal opprette ny komplett stonad`() {

    Mockito.`when`(persistenceServiceMock.opprettStonad(MockitoHelper.capture(opprettStonadRequestDto)))
      .thenReturn(1)
      doNothing().`when`(persistenceServiceMock).opprettPeriode(MockitoHelper.capture(periodeBoCaptor), eq(1))

    val nyStonadOpprettetStonadId = stonadService.opprettStonad(byggStonadRequest())

    val stonadDto = opprettStonadRequestDto.value
    val periodeDtoListe = periodeBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettStonad(MockitoHelper.any(OpprettStonadRequestDto::class.java))

/*    Mockito.verify(persistenceServiceMock, Mockito.times(2))
      .opprettNyPeriode(MockitoHelper.any(PeriodeBo::class.java), nyStonadOpprettetStonadId)*/

    assertAll(
      Executable { assertThat(nyStonadOpprettetStonadId).isNotNull() },

      // Sjekk stonadDto
      Executable { assertThat(stonadDto).isNotNull() },
      Executable { assertThat(stonadDto.type).isEqualTo(StonadType.BIDRAG) },
      Executable { assertThat(stonadDto.sakId).isEqualTo("SAK-001") },
      Executable { assertThat(stonadDto.skyldnerId).isEqualTo("01018011111") },
      Executable { assertThat(stonadDto.kravhaverId).isEqualTo("01010511111") },
      Executable { assertThat(stonadDto.mottakerId).isEqualTo("01018211111") },
      Executable { assertThat(stonadDto.opprettetAv).isEqualTo("X123456") },
      Executable { assertThat(stonadDto.indeksreguleringAar).isEqualTo("2024") },
      Executable { assertThat(stonadDto.innkreving).isEqualTo(Innkreving.JA) },


      // Sjekk PeriodeDto
      Executable { assertThat(periodeDtoListe).isNotNull() },
      Executable { assertThat(periodeDtoListe.size).isEqualTo(2) },
      Executable { assertThat(periodeDtoListe[0].periodeFom).isEqualTo(LocalDate.parse("2019-01-01")) },
      Executable { assertThat(periodeDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2019-07-01")) },
      Executable { assertThat(periodeDtoListe[0].vedtakId).isEqualTo(321) },
      Executable { assertThat(periodeDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(3490)) },
      Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },

      Executable { assertThat(periodeDtoListe[1].periodeFom).isEqualTo(LocalDate.parse("2019-07-01")) },
      Executable { assertThat(periodeDtoListe[1].periodeTil).isEqualTo(LocalDate.parse("2020-01-01")) },
      Executable { assertThat(periodeDtoListe[1].vedtakId).isEqualTo(323) },
      Executable { assertThat(periodeDtoListe[1].belop).isEqualTo(BigDecimal.valueOf(3520)) },
      Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") }

    )
  }

  object MockitoHelper {
    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
