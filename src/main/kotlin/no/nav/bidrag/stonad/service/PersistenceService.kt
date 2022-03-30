package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.MottakerIdHistorikkDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.stonad.bo.MottakerIdHistorikkBo
import no.nav.bidrag.stonad.bo.PeriodeBo
import no.nav.bidrag.stonad.bo.StonadBo
import no.nav.bidrag.stonad.bo.toMottakerIdHistorikkEntity
import no.nav.bidrag.stonad.bo.toPeriodeEntity
import no.nav.bidrag.stonad.bo.toStonadEntity
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.stonad.persistence.entity.toMottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.entity.toHentStonadPeriodeDto
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

  fun opprettNyPeriode(periodeBo: PeriodeBo) {
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
    return periode.toHentStonadPeriodeDto()
  }

  fun opprettNyePerioder(periodeBoListe: List<PeriodeBo>, stonadBo: StonadBo): List<PeriodeBo> {
    val stonad = stonadBo.toStonadEntity()
    val opprettedePeriodeBoListe = mutableListOf<PeriodeBo>()
    periodeBoListe.forEach {
      val nyPeriode = it.toPeriodeEntity(stonad)
      val periode = periodeRepository.save(nyPeriode)
      opprettedePeriodeBoListe.add(periode.toHentStonadPeriodeDto())
    }
    return opprettedePeriodeBoListe
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
    val mottakerIdHistorikkBo = MottakerIdHistorikkBo(stonadId = request.stonadId,
      mottakerIdEndretFra = eksisterendeStonad.mottakerId, mottakerIdEndretTil = request.nyMottakerId,
      opprettetAv = request.opprettetAv)

    val nyMottakerIdHistorikk = mottakerIdHistorikkBo.toMottakerIdHistorikkEntity(eksisterendeStonad)
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

  fun hentPeriode(id: Int): PeriodeBo? {
    val periode = periodeRepository.findById(id)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke periode med id %d i databasen",
            id
          )
        )
      }
    return periode.toHentStonadPeriodeDto()
  }



}
