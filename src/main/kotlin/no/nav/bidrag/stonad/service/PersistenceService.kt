package no.nav.bidrag.stonad.service

import io.micrometer.core.annotation.Timed
import no.nav.bidrag.stonad.SECURE_LOGGER
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toJustertPeriodeEntity
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stønad
import no.nav.bidrag.stonad.persistence.entity.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.toStønadEntity
import no.nav.bidrag.stonad.persistence.entity.toStønadPeriodeDto
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStønadsperiodeRequestDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PersistenceService(val stønadRepository: StonadRepository, val periodeRepository: PeriodeRepository) {

    @Timed
    fun opprettStønad(opprettStønadRequestDto: OpprettStønadRequestDto): Int {
        val nyStønad = opprettStønadRequestDto.toStønadEntity()
        val stønad = stønadRepository.save(nyStønad)
        return stønad.stønadsid!!
    }

    @Timed
    fun oppdaterStønad(stønadsid: Int, opprettetAv: String) {
        stønadRepository.oppdaterStonadMedEndretAvOgTimestamp(stønadsid, opprettetAv)
    }

    fun opprettPeriode(periodeBo: PeriodeBo, stønadsid: Int) {
        val eksisterendeStonad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        val nyPeriode = periodeBo.toPeriodeEntity(eksisterendeStonad)
        periodeRepository.save(nyPeriode)
    }

    fun opprettJustertPeriode(periodeBo: PeriodeBo, stønadsid: Int, vedtakstidspunkt: LocalDateTime) {
        val eksisterendeStonad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        val nyPeriode = periodeBo.toJustertPeriodeEntity(eksisterendeStonad, vedtakstidspunkt)
        periodeRepository.save(nyPeriode)
    }

    fun opprettPerioder(periodeRequestListe: List<OpprettStønadsperiodeRequestDto>, stønadsid: Int) {
        val eksisterendeStonad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        periodeRequestListe.forEach {
            val nyPeriode = it.toPeriodeEntity(eksisterendeStonad)
            periodeRepository.save(nyPeriode)
        }
    }

    @Timed
    fun hentStønadFraId(stønadsid: Int): Stønad? {
        val stønad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        return stønad
    }

    @Timed
    fun hentStønad(stonadType: String, skyldner: String, kravhaver: String, sak: String): Stønad? =
        stønadRepository.finnStønad(stonadType, skyldner, kravhaver, sak)

    fun hentStønaderForSak(sak: String): List<Stønad> = stønadRepository.finnStønaderForSak(sak)

    @Timed
    fun finnBidragssakerForSkyldner(skyldner: String): List<Stønad> = stønadRepository.finnBidragssakerForSkyldner(skyldner)
    fun finnAlleStønaderForSkyldner(skyldner: String): List<Stønad> = stønadRepository.finnAlleStønaderForSkyldner(skyldner)

    fun hentPerioderForStønad(id: Int): List<Periode> = periodeRepository.hentGyldigePerioderForStønad(id)

    fun hentPerioderForStønadInkludertUgyldiggjorte(id: Int): List<Periode> = periodeRepository.hentPerioderForStønadInkludertUgyldiggjorte(id)

    fun endreMottaker(stønadsid: Int, nyMottaker: String, opprettetAv: String) {
        SECURE_LOGGER.info("Oppdaterer mottaker for stønadsid: $stønadsid")
        stønadRepository.endreMottakerForStønad(stønadsid, nyMottaker, opprettetAv)
    }

    fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtaksid: Int, vedtakstidspunkt: LocalDateTime) {
        periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtaksid, vedtakstidspunkt)
    }

    fun hentPeriode(id: Int): StønadPeriodeDto? {
        val periode =
            periodeRepository.findById(id)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke periode med id %d i databasen",
                            id,
                        ),
                    )
                }
        return periode.toStønadPeriodeDto()
    }

    fun hentPerioderForStønadForAngittTidspunkt(id: Int, gyldigTidspunkt: LocalDateTime): List<Periode> =
        periodeRepository.hentGyldigePerioderForStønadForAngittTidspunkt(id, gyldigTidspunkt)
}
