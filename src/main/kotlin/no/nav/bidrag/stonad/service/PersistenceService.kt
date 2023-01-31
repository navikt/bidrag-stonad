package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.stonad.SECURE_LOGGER
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.toStonadPeriodeDto
import no.nav.bidrag.stonad.persistence.entity.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.toStonadEntity
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PersistenceService(
  val stonadRepository: StonadRepository,
  val periodeRepository: PeriodeRepository,
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyStonad(opprettStonadRequestDto: OpprettStonadRequestDto): Int {
    val nyStonad = opprettStonadRequestDto.toStonadEntity()
    val stonad = stonadRepository.save(nyStonad)
    return stonad.stonadId
  }

  fun oppdaterStonad(stonadId: Int, opprettetAv: String) {
    stonadRepository.oppdaterStonadMedEndretAvOgTimestamp(stonadId, opprettetAv)
  }

  fun opprettNyPeriode(periodeBo: PeriodeBo, stonadId: Int) {
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

  fun opprettNyePerioder(periodeRequestListe: List<OpprettStonadPeriodeRequestDto>, stonadId: Int) {
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

  fun hentStonad(stonadType: String, skyldnerId: String, kravhaverId: String, sakId: String): Stonad? {
    return stonadRepository.finnStonad(stonadType, skyldnerId, kravhaverId, sakId)
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

  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int, opprettetTidspunkt: LocalDateTime) {
    periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtakId, opprettetTidspunkt)
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



}
