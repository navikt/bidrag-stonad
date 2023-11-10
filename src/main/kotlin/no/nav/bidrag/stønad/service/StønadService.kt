package no.nav.bidrag.stønad.service

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.stønad.bo.OppdatertPeriode
import no.nav.bidrag.stønad.bo.PeriodeBo
import no.nav.bidrag.stønad.bo.toPeriodeBo
import no.nav.bidrag.stønad.controller.StønadController
import no.nav.bidrag.stønad.persistence.entity.toStønadDto
import no.nav.bidrag.stønad.persistence.entity.toStønadPeriodeDto
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth

@Service
@Transactional
class StønadService(val persistenceService: PersistenceService) {

    private val LOGGER = LoggerFactory.getLogger(StønadController::class.java)

    // Opprett komplett stønad (alle tabeller)
    fun opprettStonad(stønadRequest: OpprettStønadRequestDto): Int {
        val opprettetStonadId = persistenceService.opprettStønad(stønadRequest)
        // Perioder
        stønadRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadId) }
        return opprettetStonadId
    }

    // Opprett periode
    private fun opprettPeriode(periodeRequest: OpprettStønadsperiodeRequestDto, stønadsid: Int) {
        persistenceService.opprettPeriode(periodeRequest.toPeriodeBo(), stønadsid)
    }

    // Henter stønad ut fra stønadsid
    fun hentStonadFraId(stønadsid: Int): StønadDto? {
        val stønad = persistenceService.hentStønadFraId(stønadsid)
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe = persistenceService.hentPerioderForStønad(stønadsid)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    // Henter stønad ut fra unik nøkkel for stønad
    fun hentStønad(request: HentStønadRequest): StønadDto? {
        val stønad = persistenceService.hentStønad(request.type.toString(), request.skyldner.verdi, request.kravhaver.verdi, request.sak.toString())
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe = persistenceService.hentPerioderForStønad(stønad.stønadsid)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    fun hentStønadInkludertUgyldiggjortePerioder(
        stønadstype: String,
        skyldner: String,
        kravhaver: String,
        sak: String,
    ): StønadDto? {
        val stønad = persistenceService.hentStønad(stønadstype, skyldner, kravhaver, sak)
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe =
                persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(stønad.stønadsid)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    fun hentStønadHistorisk(request: HentStønadHistoriskRequest): StønadDto? {
        val stonad = persistenceService.hentStønad(request.type.toString(), request.skyldner.verdi, request.kravhaver.verdi, request.sak.toString())
        if (stonad != null) {
            val stonadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe =
                persistenceService.hentPerioderForStønadForAngittTidspunkt(stonad.stønadsid, request.gyldigTidspunkt)
            periodeListe.forEach { periode ->
                stonadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stonad.toStønadDto(stonadPeriodeDtoListe)
        } else {
            return null
        }
    }

    // Henter alle stønad for angitt sak
    fun hentStønaderForSak(sak: String): List<StønadDto> {
        val stønadListe = persistenceService.hentStønaderForSak(sak)
        if (stønadListe.isNotEmpty()) {
            val stønadsendringDtoListe = mutableListOf<StønadDto>()
            stønadListe.forEach { stønad ->
                val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
                val periodeListe = persistenceService.hentPerioderForStønad(stønad.stønadsid)
                periodeListe.forEach { periode ->
                    stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
                }
                stønadsendringDtoListe.add(stønad.toStønadDto(stønadPeriodeDtoListe))
            }
            return stønadsendringDtoListe
        } else {
            return emptyList()
        }
    }

    fun endreStonad(eksisterendeStonad: StønadDto, oppdatertStonad: OpprettStønadRequestDto, vedtakstidspunkt: LocalDateTime) {
        val stønadsid = eksisterendeStonad.stønadsid
        val endretAvSaksbehandlerId = oppdatertStonad.opprettetAv

        persistenceService.oppdaterStønad(stønadsid, endretAvSaksbehandlerId)

        val oppdatertStonadVedtakId = oppdatertStonad.periodeListe.first().vedtaksid

        eksisterendeStonad.periodeListe.forEach { periode ->
            val justertPeriode = finnOverlappPeriode(periode.toPeriodeBo(), oppdatertStonad)
            if (justertPeriode.settPeriodeSomUgyldig) {
                // Setter opprinnelige periode som ugyldig
                persistenceService.settPeriodeSomUgyldig(periode.periodeid, oppdatertStonadVedtakId, vedtakstidspunkt)
            }
            // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
            if (justertPeriode.oppdaterPerioder) {
                justertPeriode.periodeListe.forEach {
                    persistenceService.opprettJustertPeriode(it, stønadsid, vedtakstidspunkt)
                }
            }
        }

        oppdatertStonad.periodeListe.forEach {
            // Sjekk om beløp for ny periode = null, det er da et opphørsvedtak og periode skal ikke lagres.
            // Sjekken må gjøres etter at de eksisterende periodene er behandlet
            if (it.beløp != null) {
                persistenceService.opprettPeriode(it.toPeriodeBo(), stønadsid)
            }
        }
    }

    fun finnOverlappPeriode(eksisterendePeriode: PeriodeBo, oppdatertStonad: OpprettStønadRequestDto): OppdatertPeriode {
        val periodeBoListe = mutableListOf<PeriodeBo>()
        val oppdatertStønadDatoFom = oppdatertStonad.periodeListe.first().periode.fom
        val oppdatertStønadDatoTil = oppdatertStonad.periodeListe.last().periode.til
        if (eksisterendePeriode.periode.fom.isBefore(oppdatertStønadDatoFom)) {
            if (eksisterendePeriode.periode.til == null || eksisterendePeriode.periode.til!!.isAfter(oppdatertStønadDatoFom)) {
                // Perioden overlapper. Eksisterende periode må settes som ugyldig og ny periode opprettes med korrigert til-dato.
                periodeBoListe.add(lagNyPeriodeMedEndretTilDato(eksisterendePeriode, oppdatertStønadDatoFom))
                if (oppdatertStønadDatoTil != null && (eksisterendePeriode.periode.til == null || eksisterendePeriode.periode.til!!.isAfter(oppdatertStønadDatoTil))) {
                    periodeBoListe.add(lagNyPeriodeMedEndretFomDato(eksisterendePeriode, oppdatertStønadDatoTil))
                }
                return OppdatertPeriode(periodeBoListe, true, true)
            }
        } else if (oppdatertStønadDatoTil == null) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeBoListe, false, true)
        } else if (eksisterendePeriode.periode.fom.isAfter(oppdatertStønadDatoTil.minusMonths(1))) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeBoListe, false, false)
        } else if (eksisterendePeriode.periode.til == null || eksisterendePeriode.periode.til!!.isAfter(oppdatertStønadDatoTil)) {
            periodeBoListe.add(lagNyPeriodeMedEndretFomDato(eksisterendePeriode, oppdatertStønadDatoTil))
            return OppdatertPeriode(periodeBoListe, true, true)
        } else if (eksisterendePeriode.periode.til!!.isBefore(oppdatertStønadDatoTil.plusMonths(1))) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeBoListe, false, true)
        } else {
            periodeBoListe.add(eksisterendePeriode)
        }
        return OppdatertPeriode(periodeBoListe, false, false)
    }

    fun lagNyPeriodeMedEndretFomDato(periode: PeriodeBo, nyFomDato: YearMonth): PeriodeBo {
//    persistenceService.opprettNyPeriode(
        return PeriodeBo(
            periode = ÅrMånedsperiode(nyFomDato, periode.periode.til),
            stønadsid = periode.stønadsid,
            vedtaksid = periode.vedtaksid,
            periodeGjortUgyldigAvVedtaksid = null,
            beløp = periode.beløp,
            valutakode = periode.valutakode,
            resultatkode = periode.resultatkode,
        )
    }

    fun lagNyPeriodeMedEndretTilDato(periode: PeriodeBo, nyTilDato: YearMonth): PeriodeBo {
//    persistenceService.opprettNyPeriode(
        return PeriodeBo(
            periode = ÅrMånedsperiode(periode.periode.fom, til = nyTilDato),
            stønadsid = periode.stønadsid,
            vedtaksid = periode.vedtaksid,
            periodeGjortUgyldigAvVedtaksid = null,
            beløp = periode.beløp,
            valutakode = periode.valutakode,
            resultatkode = periode.resultatkode,
        )
    }
}
