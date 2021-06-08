package no.nav.bidrag.stonad.service

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

  fun finnStonadFraId(stonadId: Int): StonadDto {
    val stonad = stonadRepository.findById(stonadId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", stonadId)) }
    return stonad.toStonadDto()
  }

  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String): StonadDto {
    val stonad = stonadRepository.hentStonad(stonadType, skyldnerId, kravhaverId)
//      .orElseThrow { IllegalArgumentException(String
//        .format("Fant ikke stønad med id %d i databasen", stonadType, skyldnerId, kravhaverId)) }
    return stonad.toStonadDto()
  }

  fun opprettNyMottakerIdHistorikk(dto: MottakerIdHistorikkDto): MottakerIdHistorikkDto {
    val eksisterendeStonad = stonadRepository.findById(dto.stonadId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", dto.stonadId)) }
    val nyMottakerIdHistorikk = dto.toMottakerIdHistorikkEntity(eksisterendeStonad)
    val mottakerIdHistorikk = mottakerIdHistorikkRepository.save(nyMottakerIdHistorikk)
    return mottakerIdHistorikk.toMottakerIdHistorikkDto()
  }

  fun finnAlleEndringerAvMottakerIdForStonad(id: Int): List<MottakerIdHistorikkDto> {
    val mottakerIdHistorikkDtoListe = mutableListOf<MottakerIdHistorikkDto>()
    mottakerIdHistorikkRepository.hentAlleMottakerIdHistorikkForStonad(id)
      .forEach {mottakerIdHistorikk -> mottakerIdHistorikkDtoListe.add(mottakerIdHistorikk.toMottakerIdHistorikkDto()) }
    return mottakerIdHistorikkDtoListe
  }

  fun opprettNyPeriode(dto: PeriodeDto): PeriodeDto {
    val eksisterendeStonad = stonadRepository.findById(dto.stonadId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonad med id %d i databasen", dto.stonadId)) }
    val nyPeriode = dto.toPeriodeEntity(eksisterendeStonad)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun finnPeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", id)) }
    return periode.toPeriodeDto()
  }

  fun finnAllePerioderForStonad(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.hentAllePerioderForStonad(id)
      .forEach {periode -> periodeDtoListe.add(periode.toPeriodeDto())}

    return periodeDtoListe
  }

}
