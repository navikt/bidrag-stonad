package no.nav.bidrag.stonad.service

import io.micrometer.core.annotation.Timed
import no.nav.bidrag.stonad.SECURE_LOGGER
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toJustertPeriodeEntity
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.toStonadEntity
import no.nav.bidrag.stonad.persistence.entity.toStonadPeriodeDto
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.bidrag.transport.behandling.stonad.response.StonadPeriodeDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStonadRequestDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PersistenceService(
    val stonadRepository: StonadRepository,
    val periodeRepository: PeriodeRepository
) {

    private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

    @Timed
    fun opprettStonad(opprettStonadRequestDto: OpprettStonadRequestDto): Int {
        val nyStonad = opprettStonadRequestDto.toStonadEntity()
        val stonad = stonadRepository.save(nyStonad)
        return stonad.stonadId
    }

    @Timed
    fun oppdaterStonad(stonadId: Int, opprettetAv: String) {
        stonadRepository.oppdaterStonadMedEndretAvOgTimestamp(stonadId, opprettetAv)
    }

    fun opprettPeriode(periodeBo: PeriodeBo, stonadId: Int) {
        val eksisterendeStonad = stonadRepository.findById(stonadId)
            .orElseThrow {
                IllegalArgumentException(
                    String.format(
                        "Fant ikke stønad med id %d i databasen",
                        stonadId
                    )
                )
            }
        val nyPeriode = periodeBo.toPeriodeEntity(eksisterendeStonad)
        periodeRepository.save(nyPeriode)
    }

    fun opprettJustertPeriode(periodeBo: PeriodeBo, stonadId: Int, vedtakTidspunkt: LocalDateTime) {
        val eksisterendeStonad = stonadRepository.findById(stonadId)
            .orElseThrow {
                IllegalArgumentException(
                    String.format(
                        "Fant ikke stønad med id %d i databasen",
                        stonadId
                    )
                )
            }
        val nyPeriode = periodeBo.toJustertPeriodeEntity(eksisterendeStonad, vedtakTidspunkt)
        periodeRepository.save(nyPeriode)
    }

    fun opprettPerioder(periodeRequestListe: List<OpprettStonadPeriodeRequestDto>, stonadId: Int) {
        val eksisterendeStonad = stonadRepository.findById(stonadId)
            .orElseThrow {
                IllegalArgumentException(
                    String.format(
                        "Fant ikke stønad med id %d i databasen",
                        stonadId
                    )
                )
            }
        periodeRequestListe.forEach {
            val nyPeriode = it.toPeriodeEntity(eksisterendeStonad)
            periodeRepository.save(nyPeriode)
        }
    }

    @Timed
    fun hentStonadFraId(stonadId: Int): Stonad? {
        val stonad = stonadRepository.findById(stonadId)
            .orElseThrow {
                IllegalArgumentException(
                    String.format(
                        "Fant ikke stønad med id %d i databasen",
                        stonadId
                    )
                )
            }
        return stonad
    }

    @Timed
    fun hentStonad(stonadType: String, skyldnerId: String, kravhaverId: String, sakId: String): Stonad? {
        return stonadRepository.finnStonad(stonadType, skyldnerId, kravhaverId, sakId)
    }

    fun hentStonaderForSakId(sakId: String): List<Stonad> {
        return stonadRepository.finnStonaderForSakId(sakId)
    }

    fun hentPerioderForStonad(id: Int): List<Periode> {
        return periodeRepository.hentPerioderForStonad(id)
    }

    fun hentPerioderForStonadInkludertUgyldiggjorte(id: Int): List<Periode> {
        return periodeRepository.hentPerioderForStonadInkludertUgyldiggjorte(id)
    }

    fun endreMottakerId(stonadId: Int, nyMottakerId: String, opprettetAv: String) {
        SECURE_LOGGER.info("Oppdaterer mottakerId for stonadId: $stonadId")
        stonadRepository.endreMottakerIdForStonad(stonadId, nyMottakerId, opprettetAv)
    }

    fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int, vedtakTidspunkt: LocalDateTime) {
        periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtakId, vedtakTidspunkt)
    }

    fun hentPeriode(id: Int): StonadPeriodeDto? {
        val periode = periodeRepository.findById(id)
            .orElseThrow {
                IllegalArgumentException(
                    String.format(
                        "Fant ikke periode med id %d i databasen",
                        id
                    )
                )
            }
        return periode.toStonadPeriodeDto()
    }

    fun hentPerioderForStonadForAngittTidspunkt(id: Int, gyldigTidspunkt: LocalDateTime): List<Periode> {
        return periodeRepository.hentGyldigePerioderForStonadForAngittTidspunkt(id, gyldigTidspunkt)
    }
}
