package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.dto.GrunnlagDto
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.dto.PeriodeGrunnlagDto
import no.nav.bidrag.stonad.dto.StonadsendringDto
import no.nav.bidrag.stonad.dto.stonadDto
import no.nav.bidrag.stonad.dto.toGrunnlagEntity
import no.nav.bidrag.stonad.dto.toPeriodeEntity
import no.nav.bidrag.stonad.dto.toPeriodeGrunnlagEntity
import no.nav.bidrag.stonad.dto.toStonadsendringEntity
import no.nav.bidrag.stonad.dto.tostonadEntity
import no.nav.bidrag.stonad.persistence.entity.toGrunnlagDto
import no.nav.bidrag.stonad.persistence.entity.toPeriodeDto
import no.nav.bidrag.stonad.persistence.entity.toPeriodeGrunnlagDto
import no.nav.bidrag.stonad.persistence.entity.toStonadsendringDto
import no.nav.bidrag.stonad.persistence.entity.tostonadDto
import no.nav.bidrag.stonad.persistence.repository.GrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadsendringRepository
import no.nav.bidrag.stonad.persistence.repository.stonadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val stonadRepository: stonadRepository,
  val stonadsendringRepository: StonadsendringRepository,
  val periodeRepository: PeriodeRepository,
  val grunnlagRepository: GrunnlagRepository,
  val periodeGrunnlagRepository: PeriodeGrunnlagRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyttstonad(dto: stonadDto): stonadDto {
    val nyttstonad = dto.tostonadEntity()
    val stonad = stonadRepository.save(nyttstonad)
    return stonad.tostonadDto()
  }

  fun finnEttstonad(id: Int): stonadDto {
    val stonad = stonadRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke stonad med id %d i databasen", id)) }
    return stonad.tostonadDto()
  }

  fun finnAllestonad(): List<stonadDto> {
    val stonadDtoListe = mutableListOf<stonadDto>()
    stonadRepository.findAll().forEach { stonadDtoListe.add(it.tostonadDto()) }
    return stonadDtoListe
  }

  fun opprettNyStonadsendring(dto: StonadsendringDto): StonadsendringDto {
    val eksisterendestonad = stonadRepository.findById(dto.stonadId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonad med id %d i databasen", dto.stonadId)) }
    val nyStonadsendring = dto.toStonadsendringEntity(eksisterendestonad)
    val stonadsendring = stonadsendringRepository.save(nyStonadsendring)
    return stonadsendring.toStonadsendringDto()
  }

  fun finnEnStonadsendring(id: Int): StonadsendringDto {
    val stonadsendring = stonadsendringRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", id)) }
    return stonadsendring.toStonadsendringDto()
  }

  fun finnAlleStonadsendringerForstonad(id: Int): List<StonadsendringDto> {
    val stonadsendringDtoListe = mutableListOf<StonadsendringDto>()
    stonadsendringRepository.hentAlleStonadsendringerForstonad(id)
      .forEach {stonadsendring -> stonadsendringDtoListe.add(stonadsendring.toStonadsendringDto()) }
    return stonadsendringDtoListe
  }

  fun opprettNyPeriode(dto: PeriodeDto): PeriodeDto {
    val eksisterendeStonadsendring = stonadsendringRepository.findById(dto.stonadsendringId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonadsendring med id %d i databasen", dto.stonadsendringId)) }
    val nyPeriode = dto.toPeriodeEntity(eksisterendeStonadsendring)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun finnPeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", id)) }
    return periode.toPeriodeDto()
  }

  fun finnAllePerioderForStonadsendring(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.hentAllePerioderForStonadsendring(id)
      .forEach {periode -> periodeDtoListe.add(periode.toPeriodeDto())}

    return periodeDtoListe
  }

  fun opprettNyttGrunnlag(dto: GrunnlagDto): GrunnlagDto {
    val eksisterendestonad = stonadRepository.findById(dto.stonadId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonad med id %d i databasen", dto.stonadId)) }
    val nyttGrunnlag = dto.toGrunnlagEntity(eksisterendestonad)
    val grunnlag = grunnlagRepository.save(nyttGrunnlag)
    return grunnlag.toGrunnlagDto()
  }

  fun finnGrunnlag(id: Int): GrunnlagDto {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag.toGrunnlagDto()
  }

  fun finnAlleGrunnlagForstonad(id: Int): List<GrunnlagDto> {
    val grunnlagDtoListe = mutableListOf<GrunnlagDto>()
    grunnlagRepository.hentAlleGrunnlagForstonad(id)
      .forEach {grunnlag -> grunnlagDtoListe.add(grunnlag.toGrunnlagDto()) }
    return grunnlagDtoListe
  }

  fun opprettNyttPeriodeGrunnlag(dto: PeriodeGrunnlagDto): PeriodeGrunnlagDto {
    val eksisterendePeriode = periodeRepository.findById(dto.periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", dto.periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttPeriodeGrunnlag = dto.toPeriodeGrunnlagEntity(eksisterendePeriode, eksisterendeGrunnlag)
    LOGGER.info("nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    val periodeGrunnlag = periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun finnPeriodeGrunnlag(periodeId: Int, grunnlagId: Int): PeriodeGrunnlagDto {
    val periodeGrunnlag = periodeGrunnlagRepository.hentPeriodeGrunnlag(periodeId, grunnlagId)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun finnAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlagDto> {
    val periodeGrunnlagDtoListe = mutableListOf<PeriodeGrunnlagDto>()
    periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
      .forEach {periodeGrunnlag -> periodeGrunnlagDtoListe.add(periodeGrunnlag.toPeriodeGrunnlagDto()) }

    return periodeGrunnlagDtoListe
  }
}
