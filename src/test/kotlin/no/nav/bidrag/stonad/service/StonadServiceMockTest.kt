package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.TestUtil.Companion.byggPeriodeDto
import no.nav.bidrag.stonad.TestUtil.Companion.byggStonadDto
import no.nav.bidrag.stonad.TestUtil.Companion.byggStonadRequest
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
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
  private lateinit var stonadDtoCaptor: ArgumentCaptor<StonadDto>

  @Captor
  private lateinit var periodeDtoCaptor: ArgumentCaptor<PeriodeDto>

  @Test
  fun `skal opprette ny komplett stonad`() {

    Mockito.`when`(persistenceServiceMock.opprettNyStonad(MockitoHelper.capture(stonadDtoCaptor)))
      .thenReturn(byggStonadDto())
    Mockito.`when`(persistenceServiceMock.opprettNyPeriode(MockitoHelper.capture(periodeDtoCaptor)))
      .thenReturn(byggPeriodeDto())

    val nyStonadOpprettet = stonadService.opprettStonad(byggStonadRequest())

    val stonadDto = stonadDtoCaptor.value
    val periodeDtoListe = periodeDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyStonad(MockitoHelper.any(StonadDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettNyPeriode(MockitoHelper.any(PeriodeDto::class.java))

    assertAll(
      Executable { assertThat(nyStonadOpprettet).isNotNull() },
      Executable { assertThat(nyStonadOpprettet.stonadId).isNotNull() },

      // Sjekk stonadDto
      Executable { assertThat(stonadDto).isNotNull() },
      Executable { assertThat(stonadDto.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(stonadDto.sakId).isEqualTo("SAK-001") },
      Executable { assertThat(stonadDto.skyldnerId).isEqualTo("01018011111") },
      Executable { assertThat(stonadDto.kravhaverId).isEqualTo("01010511111") },
      Executable { assertThat(stonadDto.mottakerId).isEqualTo("01018211111") },
      Executable { assertThat(stonadDto.opprettetAvSaksbehandlerId).isEqualTo("X123456") },


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
