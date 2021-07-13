package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Periode
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?>{

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId and pe.periodeGjortUgyldigAvVedtakId IS NULL")
  fun finnPerioderForStonad(stonadId: Int): List<Periode>

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId")
  fun finnPerioderForStonadInkludertUgyldige(stonadId: Int): List<Periode>


@Query(
  "update Periode pe set pe.periodeGjortUgyldigAvVedtakId = :periodeGjortUgyldigAvVedtakId where pe.periodeId = :periodeId")
  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int)
}
