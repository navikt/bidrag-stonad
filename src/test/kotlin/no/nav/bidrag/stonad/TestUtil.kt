package no.nav.bidrag.stonad

import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.bo.PeriodeBo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggStonadRequest() = OpprettStonadRequestDto(
      type = StonadType.BIDRAG,
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      indeksreguleringAar = "2024",
      innkreving = Innkreving.JA,
      opprettetAv = "X123456",
      periodeListe = listOf(
        OpprettStonadPeriodeRequestDto(
          periodeFom = LocalDate.parse("2019-01-01"),
          periodeTil = LocalDate.parse("2019-07-01"),
          vedtakId = 321,
          periodeGjortUgyldigAvVedtakId = null,
          belop = BigDecimal.valueOf(3490),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
        OpprettStonadPeriodeRequestDto(
          periodeFom = LocalDate.parse("2019-07-01"),
          periodeTil = LocalDate.parse("2020-01-01"),
          vedtakId = 323,
          periodeGjortUgyldigAvVedtakId = null,
          belop = BigDecimal.valueOf(3520),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG")
      )
    )


    fun byggStonadDto() = StonadDto(
      stonadId = (1..100).random(),
      type = StonadType.BIDRAG,
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      indeksreguleringAar = "2024",
      innkreving = Innkreving.JA,
      opprettetAv = "X123456",
      opprettetTimestamp = LocalDateTime.now(),
      endretAv = "X123456",
      endretTimestamp = LocalDateTime.now(),
      listOf(
        StonadPeriodeDto(
          periodeId = (1..100).random(),
          periodeFom = LocalDate.parse("2019-01-01"),
          periodeTil = LocalDate.parse("2019-07-01"),
          stonadId = (1..100).random(),
          vedtakId = 321,
          periodeGjortUgyldigAvVedtakId = 246,
          belop = BigDecimal.valueOf(3490),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG")
      )
    )

    fun byggPeriodeDto() = PeriodeBo(
      periodeId = (1..100).random(),
      periodeFom = LocalDate.parse("2019-07-01"),
      periodeTil = LocalDate.parse("2020-01-01"),
      stonadId = (1..100).random(),
      vedtakId = (1..100).random(),
      periodeGjortUgyldigAvVedtakId = (1..100).random(),
      belop = BigDecimal.valueOf(3520),
      valutakode = "NOK",
      resultatkode = "KOSTNADSBEREGNET_BIDRAG"
    )

  }
}
