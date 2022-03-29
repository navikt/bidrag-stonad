package no.nav.bidrag.stonad

import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.api.OpprettPeriodeRequest
import no.nav.bidrag.stonad.api.OpprettStonadRequest
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.StonadBo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggStonadRequest() = OpprettStonadRequest(
      stonadType = StonadType.BIDRAG,
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAv = "X123456",
      endretAv =  "X654321",
      periodeListe = listOf(
        OpprettPeriodeRequest(
          periodeFom = LocalDate.parse("2019-01-01"),
          periodeTil = LocalDate.parse("2019-07-01"),
          stonadId = 0,
          vedtakId = 321,
          belop = BigDecimal.valueOf(3490),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
        OpprettPeriodeRequest(
          periodeFom = LocalDate.parse("2019-07-01"),
          periodeTil = LocalDate.parse("2020-01-01"),
          stonadId = 0,
          vedtakId = 323,
          belop = BigDecimal.valueOf(3520),
          valutakode = "NOK",
          resultatkode = "KOSTNADSBEREGNET_BIDRAG")
      )
    )


    fun byggStonadDto() = StonadBo(
      stonadId = (1..100).random(),
      stonadType = "BIDRAG",
      sakId = "SAK-001",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111",
      opprettetAv = "X123456",
      opprettetTimestamp = LocalDateTime.now(),
      endretAv = "X654321",
      endretTimestamp = LocalDateTime.now()
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
