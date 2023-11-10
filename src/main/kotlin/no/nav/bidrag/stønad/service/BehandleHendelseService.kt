package no.nav.bidrag.stønad.service

import no.nav.bidrag.domene.enums.Beslutningstype
import no.nav.bidrag.domene.enums.Innkrevingstype
import no.nav.bidrag.domene.enums.Vedtakstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.SECURE_LOGGER
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Stønadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth

private val LOGGER = LoggerFactory.getLogger(DefaultBehandleHendelseService::class.java)

interface BehandleHendelseService {
    fun behandleHendelse(vedtakHendelse: VedtakHendelse)
}

@Service
@Transactional
class DefaultBehandleHendelseService(
    private val stønadService: StønadService,
    private val persistenceService: PersistenceService,
) : BehandleHendelseService {

    override fun behandleHendelse(vedtakHendelse: VedtakHendelse) {
        SECURE_LOGGER.info("Behandler vedtakHendelse: $vedtakHendelse")

        vedtakHendelse.stønadsendringListe?.forEach() { stønadsendring ->
            behandleVedtakHendelse(stønadsendring, vedtakHendelse.type, vedtakHendelse.id, vedtakHendelse.opprettetAv, vedtakHendelse.vedtakstidspunkt)
        }
    }

    private fun behandleVedtakHendelse(
        stønadsendring: Stønadsendring,
        vedtakType: Vedtakstype,
        vedtaksid: Int,
        opprettetAv: String,
        vedtakstidspunkt: LocalDateTime,
    ) {
//    Sjekker om stønad skal oppdateres
        if (stønadsendring.beslutning == Beslutningstype.ENDRING && stønadsendring.innkreving == Innkrevingstype.MED_INNKREVING) {
            val eksisterendeStonad = stønadService.hentStønad(
                HentStønadRequest(stønadsendring.type, stønadsendring.sak, stønadsendring.skyldner, stønadsendring.kravhaver),
            )

            if (eksisterendeStonad != null) {
                if (vedtakType == Vedtakstype.ENDRING_MOTTAKER) {
                    // Mottatt hendelse skal oppdatere mottaker for alle stønader i stønadsendringListe. Ingen perioder skal oppdateres.
                    persistenceService.endreMottaker(eksisterendeStonad.stønadsid, stønadsendring.mottaker.verdi, opprettetAv)
                } else {
                    // Mottatt Hendelse skal oppdatere eksisterende stønad
                    endreStonad(eksisterendeStonad, stønadsendring, vedtaksid, opprettetAv, vedtakstidspunkt)
                }
            } else {
                // Stønaden finnes ikke fra , hvis det er forsøkt endret mottaker for stønad som ikke finnes så skal det logges, men ikke feile.
                if (vedtakType == Vedtakstype.ENDRING_MOTTAKER) {
                    SECURE_LOGGER.info("Mottaker forsøkt endret for stønad som ikke finnes $vedtaksid")
                } else {
                    opprettStønad(stønadsendring, vedtaksid, opprettetAv, vedtakstidspunkt)
                }
            }
        } else {
            SECURE_LOGGER.info("Stønad ikke oppdatert pga innkreving = UTEN_INNKREVING eller beslutning = STADFESTELSE eller AVVISTfalse: $vedtaksid")
        }
    }

    private fun endreStonad(
        eksisterendeStonad: StønadDto,
        stønadsendring: Stønadsendring,
        vedtaksid: Int,
        opprettetAv: String,
        vedtakstidspunkt: LocalDateTime,
    ) {
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        val hendelsePeriodeListe = stønadsendring.periodeListe.sortedBy { it.periode.fom }
        var i = 1
        hendelsePeriodeListe.forEach {
            periodeListe.add(
                OpprettStønadsperiodeRequestDto(
                    periode = ÅrMånedsperiode(it.periode.fom, finnPeriodeTil(it.periode.til, hendelsePeriodeListe, i)),
                    vedtaksid = vedtaksid,
                    gyldigFra = vedtakstidspunkt,
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = it.beløp,
                    valutakode = it.valutakode,
                    resultatkode = it.resultatkode,
                ),
            )
            i++
        }

        val oppdatertStonad =
            OpprettStønadRequestDto(
                type = stønadsendring.type,
                sak = stønadsendring.sak,
                skyldner = stønadsendring.skyldner,
                kravhaver = stønadsendring.kravhaver,
                mottaker = stønadsendring.mottaker,
                førsteIndeksreguleringsår = stønadsendring.førsteIndeksreguleringsår,
                innkreving = stønadsendring.innkreving,
                opprettetAv = opprettetAv,
                periodeListe = periodeListe,
            )

        stønadService.endreStonad(eksisterendeStonad, oppdatertStonad, vedtakstidspunkt)
    }

    private fun opprettStønad(stønadsendring: Stønadsendring, vedtaksid: Int, opprettetAv: String, vedtakstidspunkt: LocalDateTime) {
        val periodeListe = mutableListOf<OpprettStønadsperiodeRequestDto>()
        val hendelsePeriodeListe = stønadsendring.periodeListe.sortedBy { it.periode.fom }
        var i = 1
        hendelsePeriodeListe.forEach { periode ->
            // Kun perioder med beløp skal lagres
            if (periode.beløp != null) {
                periodeListe.add(
                    OpprettStønadsperiodeRequestDto(
                        periode = ÅrMånedsperiode(periode.periode.fom, finnPeriodeTil(periode.periode.til, hendelsePeriodeListe, i)),
                        vedtaksid = vedtaksid,
                        gyldigFra = vedtakstidspunkt,
                        gyldigTil = null,
                        periodeGjortUgyldigAvVedtaksid = null,
                        beløp = periode.beløp,
                        valutakode = periode.valutakode,
                        resultatkode = periode.resultatkode,
                    ),
                )
            }
            i++
        }

        // Hvis periodelisten er tom (kun perioder med beløp = null) så skal stønaden ikke opprettes
        if (periodeListe.isNotEmpty()) {
            stønadService.opprettStonad(
                OpprettStønadRequestDto(
                    type = stønadsendring.type,
                    sak = stønadsendring.sak,
                    skyldner = stønadsendring.skyldner,
                    kravhaver = stønadsendring.kravhaver,
                    mottaker = stønadsendring.mottaker,
                    førsteIndeksreguleringsår = stønadsendring.førsteIndeksreguleringsår,
                    innkreving = stønadsendring.innkreving,
                    opprettetAv = opprettetAv,
                    periodeListe = periodeListe,
                ),
            )
        }
    }

    private fun finnPeriodeTil(til: YearMonth?, periodeListe: List<Periode>, i: Int): YearMonth? {
        return if (i == periodeListe.size) {
            // Siste element i listen, til skal ikke justeres
            til
        } else {
            til ?: periodeListe[i].periode.fom
        }
    }
}
