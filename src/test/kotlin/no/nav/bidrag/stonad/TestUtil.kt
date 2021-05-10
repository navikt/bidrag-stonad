package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggStonadRequest() = NyStonadRequest(
      stonadType = "BIDRAG",
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAvSaksbehandlerId = "X123456",
      endretAvSaksbehandlerId =  "X654321",
      periodeListe = listOf(
        NyPeriodeRequest(
          periodeFom = LocalDate.parse("2019-01-01"),
          periodeTil = LocalDate.parse("2019-07-01"),
          stonadId = 0,
          vedtakId = 321,
          belop = BigDecimal.valueOf(3490),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
        NyPeriodeRequest(
          periodeFom = LocalDate.parse("2019-07-01"),
          periodeTil = LocalDate.parse("2020-01-01"),
          stonadId = 0,
          vedtakId = 323,
          belop = BigDecimal.valueOf(3520),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG")
      )
    )


    fun byggStonadDto() = StonadDto(
      stonadId = (1..100).random(),
      stonadType = "BIDRAG",
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAvSaksbehandlerId = "X123456",
      opprettetTimestamp = LocalDateTime.now(),
      endretAvSaksbehandlerId = "X654321",
      endretTimestamp = LocalDateTime.now()
    )

    fun byggPeriodeDto() = PeriodeDto(
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
