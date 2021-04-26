package no.nav.bidrag.stonad

import no.nav.bidrag.stonad.api.NyttGrunnlagRequest
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.api.NyKomplettstonadRequest
import no.nav.bidrag.stonad.dto.GrunnlagDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.PeriodeGrunnlagDto
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.stonadDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggKomplettstonadRequest() = NyKomplettstonadRequest(
      saksbehandlerId = "X123456",
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe()
    )

    private fun byggGrunnlagListe() = listOf(
      NyttGrunnlagRequest(
        grunnlagReferanse = "BM-LIGS-19",
        grunnlagType = "INNTEKT"
      ),
      NyttGrunnlagRequest(
        grunnlagReferanse = "BM-LIGN-19",
        grunnlagType = "INNTEKT"
      ),
      NyttGrunnlagRequest(
        grunnlagReferanse = "BP-SKATTEKLASSE-19",
        grunnlagType = "SKATTEKLASSE"
      ),
      NyttGrunnlagRequest(
        grunnlagReferanse = "SJAB-REF001",
        grunnlagType = "SJABLON"
      )
    )

    private fun byggStonadsendringListe() = listOf(
      NyStonadRequest(
        stonadType = "BIDRAG",
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-01-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = false
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = true
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BP-SKATTEKLASSE-19",
                grunnlagValgt = true
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      ),
      NyStonadRequest(
        stonadType = "SAERTILSKUDD",
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-06-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              no.nav.bidrag.stonad.api.GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      )
    )


    fun byggstonadDto() = stonadDto(
      stonadId = (1..100).random(),
      enhetId = "4812",
      saksbehandlerId = "X123456",
      opprettetTimestamp = LocalDateTime.now()
    )

    fun byggStonadsendringDto() = StonadDto(
      stonadId = (1..100).random(),
      stonadType = "BIDRAG",
      stonadId = (1..100).random(),
      sakId = "SAK-001",
      behandlingId = "Fritekst",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111"
    )

    fun byggPeriodeDto() = PeriodeDto(
      periodeId = (1..100).random(),
      periodeFomDato = LocalDate.parse("2019-07-01"),
      periodeTilDato = LocalDate.parse("2020-01-01"),
      stonadsendringId = (1..100).random(),
      belop = BigDecimal.valueOf(3520),
      valutakode = "NOK",
      resultatkode = "KOSTNADSBEREGNET_BIDRAG"
    )

    fun byggGrunnlagDto() = GrunnlagDto(
      grunnlagId = (1..100).random(),
      grunnlagReferanse = "BM-LIGN-19",
      stonadId = (1..100).random(),
      grunnlagType = "INNTEKT",
      grunnlagInnhold = "Innhold"
    )

    fun byggPeriodeGrunnlagDto() = PeriodeGrunnlagDto(
      periodeId = (1..100).random(),
      grunnlagId = (1..100).random(),
      grunnlagValgt = true
    )
  }
}