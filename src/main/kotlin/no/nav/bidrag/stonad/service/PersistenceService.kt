package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadPeriodeDto
import no.nav.bidrag.behandling.felles.dto.stonad.MottakerIdHistorikkDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.toMottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.entity.toStonadPeriodeDto
import no.nav.bidrag.stonad.persistence.entity.toMottakerIdHistorikkEntity
import no.nav.bidrag.stonad.persistence.entity.toPeriodeEntity
import no.nav.bidrag.stonad.persistence.entity.toStonadEntity
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val stonadRepository: StonadRepository,
  val periodeRepository: PeriodeRepository,
  val mottakerIdHistorikkRepository: MottakerIdHistorikkRepository,
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

  fun hentStonad(stonadType: String, skyldnerId: String, kravhaverId: String): Stonad? {
    return stonadRepository.finnStonad(stonadType, skyldnerId, kravhaverId)
  }


  fun hentPerioderForStonad(id: Int): List<Periode> {
    return periodeRepository.hentPerioderForStonad(id)
  }


  fun hentPerioderForStonadInkludertUgyldiggjorte(id: Int): List<Periode> {
    return periodeRepository.hentPerioderForStonadInkludertUgyldiggjorte(id)
  }

  fun endreMottakerId(request: EndreMottakerIdRequestDto) {
    stonadRepository.findById(request.stonadId)
      .orElseThrow {
        IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", request.stonadId)
        )
      }
    stonadRepository.endreMottakerIdForStonad(request.stonadId, request.nyMottakerId, request.opprettetAv)
  }

  fun opprettNyMottakerIdHistorikk(request: EndreMottakerIdRequestDto): Int {
    val eksisterendeStonad = stonadRepository.findById(request.stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stønad med id %d i databasen",
            request.stonadId
          )
        )
      }

    val nyMottakerIdHistorikk = request.toMottakerIdHistorikkEntity(eksisterendeStonad, request)
    mottakerIdHistorikkRepository.save(nyMottakerIdHistorikk)
    return request.stonadId
  }

  fun hentAlleEndringerAvMottakerIdForStonad(id: Int): List<MottakerIdHistorikkDto>? {
    val mottakerIdHistorikkBoListe = mutableListOf<MottakerIdHistorikkDto>()
    mottakerIdHistorikkRepository.hentAlleMottakerIdHistorikkForStonad(id)
      .forEach { mottakerIdHistorikk -> mottakerIdHistorikkBoListe.add(mottakerIdHistorikk.toMottakerIdHistorikkDto()) }
    return mottakerIdHistorikkBoListe
  }


  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int) {
    periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtakId)
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
