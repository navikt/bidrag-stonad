package no.nav.bidrag.stønad

import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.bo.PeriodeBo
import no.nav.bidrag.transport.behandling.stonad.request.LøpendeBidragssakerRequest
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {
    companion object {
        fun byggStonadRequest() = OpprettStønadRequestDto(
            type = Stønadstype.BIDRAG,
            sak = Saksnummer("SAK-001"),
            skyldner = Personident("01018011111"),
            kravhaver = Personident("01010511111"),
            mottaker = Personident("01018211111"),
            førsteIndeksreguleringsår = 2024,
            innkreving = Innkrevingstype.MED_INNKREVING,
            opprettetAv = "X123456",
            periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 321,
                    gyldigFra = LocalDateTime.parse("2022-01-11T10:00:00.000001"),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(3490),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
                OpprettStønadsperiodeRequestDto(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                    vedtaksid = 323,
                    gyldigFra = LocalDateTime.parse("2022-01-11T10:00:00.000001"),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(3520),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            ),
        )

        fun byggStønadDto() = StønadDto(
            stønadsid = (1..100).random(),
            type = Stønadstype.BIDRAG,
            sak = Saksnummer("SAK-001"),
            skyldner = Personident("01018011111"),
            kravhaver = Personident("01010511111"),
            mottaker = Personident("01018211111"),
            førsteIndeksreguleringsår = 2024,
            innkreving = Innkrevingstype.MED_INNKREVING,
            opprettetAv = "X123456",
            opprettetTidspunkt = LocalDateTime.now(),
            endretAv = "X123456",
            endretTidspunkt = LocalDateTime.now(),
            listOf(
                StønadPeriodeDto(
                    periodeid = (1..100).random(),
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    stønadsid = (1..100).random(),
                    vedtaksid = 321,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = 246,
                    beløp = BigDecimal.valueOf(3490),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            ),
        )

        fun byggLøpendeBidragssakerRequest() = LøpendeBidragssakerRequest(
            skyldner = Personident("01010511111"),
            dato = LocalDate.now(),
        )

        fun byggPeriodeDto() = PeriodeBo(
            periodeid = (1..100).random(),
            ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
            stønadsid = (1..100).random(),
            vedtaksid = (1..100).random(),
            periodeGjortUgyldigAvVedtaksid = (1..100).random(),
            beløp = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
        )
    }
}
