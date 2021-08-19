package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Periode
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?>{

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId and pe.periodeGjortUgyldigAvVedtakId IS NULL order by pe.periodeFom")
  fun finnPerioderForStonad(stonadId: Int): List<Periode>

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId order by pe.periodeGjortUgyldigAvVedtakId asc, pe.periodeFom ")
  fun finnPerioderForStonadInkludertUgyldiggjorte(stonadId: Int): List<Periode>


  @Query(
  "update Periode pe set pe.periodeGjortUgyldigAvVedtakId = :periodeGjortUgyldigAvVedtakId where pe.periodeId = :periodeId")
  @Modifying
  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int)
}
