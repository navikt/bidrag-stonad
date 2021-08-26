package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.toMottakerIdHistorikkEntity
import no.nav.bidrag.stonad.dto.toPeriodeEntity
import no.nav.bidrag.stonad.dto.toStonadEntity
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

  fun opprettNyStonad(stonadDto: StonadDto): StonadDto {
    val nyStonad = stonadDto.toStonadEntity()
    val stonad = stonadRepository.save(nyStonad)
    return stonad.toStonadDto()
  }

  fun oppdaterStonad(stonadId: Int, endretAvSaksbehandlerId: String) {
    stonadRepository.oppdaterStonadMedEndretAvSaksbehandlerIdOgTimestamp(stonadId, endretAvSaksbehandlerId)
  }

  fun finnStonadFraId(stonadId: Int): StonadDto? {
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

  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String): StonadDto? {
    val stonad = stonadRepository.finnStonad(stonadType, skyldnerId, kravhaverId)
    return stonad?.toStonadDto()
  }

  fun endreMottakerId(stonadId: Int, nyMottakerId: String, saksbehandler: String) {
    val eksisterendeStonad = stonadRepository.findById(stonadId)
      .orElseThrow {
        IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", stonadId)
        )
      }
    stonadRepository.endreMottakerIdForStonad(stonadId, nyMottakerId, saksbehandler)
  }

  fun opprettNyMottakerIdHistorikk(request: EndreMottakerIdRequest): MottakerIdHistorikkDto {
    val eksisterendeStonad = stonadRepository.findById(request.stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stønad med id %d i databasen",
            request.stonadId
          )
        )
      }
    val mottakerIdHistorikkDto = MottakerIdHistorikkDto(stonadId = request.stonadId,
      mottakerIdEndretFra = eksisterendeStonad.mottakerId, mottakerIdEndretTil = request.nyMottakerId,
      saksbehandlerId = request.saksbehandlerId)

    val nyMottakerIdHistorikk = mottakerIdHistorikkDto.toMottakerIdHistorikkEntity(eksisterendeStonad)
    val mottakerIdHistorikk = mottakerIdHistorikkRepository.save(nyMottakerIdHistorikk)
    return mottakerIdHistorikk.toMottakerIdHistorikkDto()
  }

  fun finnAlleEndringerAvMottakerIdForStonad(id: Int): List<MottakerIdHistorikkDto>? {
    val mottakerIdHistorikkDtoListe = mutableListOf<MottakerIdHistorikkDto>()
    mottakerIdHistorikkRepository.hentAlleMottakerIdHistorikkForStonad(id)
      .forEach { mottakerIdHistorikk -> mottakerIdHistorikkDtoListe.add(mottakerIdHistorikk.toMottakerIdHistorikkDto()) }
    return mottakerIdHistorikkDtoListe
  }

  fun opprettNyPeriode(periodeDto: PeriodeDto): PeriodeDto {
    val eksisterendeStonad = stonadRepository.findById(periodeDto.stonadId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke stonad med id %d i databasen",
            periodeDto.stonadId
          )
        )
      }
    val nyPeriode = periodeDto.toPeriodeEntity(eksisterendeStonad)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun opprettNyePerioder(periodeDtoListe: List<PeriodeDto>, stonadDto: StonadDto): List<PeriodeDto> {
    val stonad = stonadDto.toStonadEntity()
    val opprettedePeriodeDtoListe = mutableListOf<PeriodeDto>()
    periodeDtoListe.forEach {
      val nyPeriode = it.toPeriodeEntity(stonad)
      val periode = periodeRepository.save(nyPeriode)
      opprettedePeriodeDtoListe.add(periode.toPeriodeDto())
    }
    return opprettedePeriodeDtoListe
  }

  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int) {
    periodeRepository.settPeriodeSomUgyldig(periodeId, periodeGjortUgyldigAvVedtakId)
  }

  fun finnPeriode(id: Int): PeriodeDto? {
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

  fun finnPerioderForStonad(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.finnPerioderForStonad(id)
      .forEach { periode -> periodeDtoListe.add(periode.toPeriodeDto()) }

    return periodeDtoListe
  }

  fun finnPerioderForStonadInkludertUgyldiggjorte(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.finnPerioderForStonadInkludertUgyldiggjorte(id)
      .forEach { periode -> periodeDtoListe.add(periode.toPeriodeDto()) }

    return periodeDtoListe
  }

}
