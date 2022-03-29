package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.bo.MottakerIdHistorikkBo
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.StonadBo
import no.nav.bidrag.stonad.bo.toMottakerIdHistorikkEntity
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.bo.toStonadEntity
import no.nav.bidrag.stonad.persistence.entity.toMottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.entity.toPeriodeDto
import no.nav.bidrag.stonad.persistence.entity.toStonadDto
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

  fun opprettNyStonad(stonadBo: StonadBo): StonadBo {
    val nyStonad = stonadBo.toStonadEntity()
    val stonad = stonadRepository.save(nyStonad)
    return stonad.toStonadDto()
  }

  fun oppdaterStonad(stonadId: Int, opprettetAv: String) {
    stonadRepository.oppdaterStonadMedEndretAvOgTimestamp(stonadId, opprettetAv)
  }

  fun hentStonadFraId(stonadId: Int): StonadBo? {
    val stonad = stonadRepository.findById(stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stønad med id %d i databasen",
            stonadId
          )
        )
      }
    return stonad.toStonadDto()
  }

  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String): StonadBo? {
    val stonad = stonadRepository.finnStonad(stonadType, skyldnerId, kravhaverId)
    return stonad?.toStonadDto()
  }

  fun endreMottakerId(stonadId: Int, nyMottakerId: String, opprettetAv: String) {
    val eksisterendeStonad = stonadRepository.findById(stonadId)
      .orElseThrow {
        IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", stonadId)
        )
      }
    stonadRepository.endreMottakerIdForStonad(stonadId, nyMottakerId, opprettetAv)
  }

  fun opprettNyMottakerIdHistorikk(request: EndreMottakerIdRequest): MottakerIdHistorikkBo {
    val eksisterendeStonad = stonadRepository.findById(request.stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stønad med id %d i databasen",
            request.stonadId
          )
        )
      }
    val mottakerIdHistorikkBo = MottakerIdHistorikkBo(stonadId = request.stonadId,
      mottakerIdEndretFra = eksisterendeStonad.mottakerId, mottakerIdEndretTil = request.nyMottakerId,
      opprettetAv = request.opprettetAv)

    val nyMottakerIdHistorikk = mottakerIdHistorikkBo.toMottakerIdHistorikkEntity(eksisterendeStonad)
    val mottakerIdHistorikk = mottakerIdHistorikkRepository.save(nyMottakerIdHistorikk)
    return mottakerIdHistorikk.toMottakerIdHistorikkDto()
  }

  fun hentAlleEndringerAvMottakerIdForStonad(id: Int): List<MottakerIdHistorikkBo>? {
    val mottakerIdHistorikkBoListe = mutableListOf<MottakerIdHistorikkBo>()
    mottakerIdHistorikkRepository.hentAlleMottakerIdHistorikkForStonad(id)
      .forEach { mottakerIdHistorikk -> mottakerIdHistorikkBoListe.add(mottakerIdHistorikk.toMottakerIdHistorikkDto()) }
    return mottakerIdHistorikkBoListe
  }

  fun opprettNyPeriode(periodeBo: PeriodeBo): PeriodeBo {
    val eksisterendeStonad = stonadRepository.findById(periodeBo.stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stonad med id %d i databasen",
            periodeBo.stonadId
          )
        )
      }
    val nyPeriode = periodeBo.toPeriodeEntity(eksisterendeStonad)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun opprettNyePerioder(periodeBoListe: List<PeriodeBo>, stonadBo: StonadBo): List<PeriodeBo> {
    val stonad = stonadBo.toStonadEntity()
    val opprettedePeriodeBoListe = mutableListOf<PeriodeBo>()
    periodeBoListe.forEach {
      val nyPeriode = it.toPeriodeEntity(stonad)
      val periode = periodeRepository.save(nyPeriode)
      opprettedePeriodeBoListe.add(periode.toPeriodeDto())
    }
    return opprettedePeriodeBoListe
  }

  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int) {
    periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtakId)
  }

  fun finnPeriode(id: Int): PeriodeBo? {
    val periode = periodeRepository.findById(id)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke periode med id %d i databasen",
            id
          )
        )
      }
    return periode.toPeriodeDto()
  }

  fun hentPerioderForStonad(id: Int): List<PeriodeBo> {
    val periodeBoListe = mutableListOf<PeriodeBo>()
    periodeRepository.finnPerioderForStonad(id)
      .forEach { periode -> periodeBoListe.add(periode.toPeriodeDto()) }

    return periodeBoListe
  }

  fun finnPerioderForStonadInkludertUgyldiggjorte(id: Int): List<PeriodeBo> {
    val periodeBoListe = mutableListOf<PeriodeBo>()
    periodeRepository.finnPerioderForStonadInkludertUgyldiggjorte(id)
      .forEach { periode -> periodeBoListe.add(periode.toPeriodeDto()) }

    return periodeBoListe
  }

}
