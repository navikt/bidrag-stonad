package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Periode
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface PeriodeRepository : CrudRepository<Periode, Int?>{

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId and pe.periodeGjortUgyldigAvVedtakId IS NULL order by pe.periodeFom")
  fun hentPerioderForStonad(stonadId: Int): List<Periode>

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId order by pe.periodeGjortUgyldigAvVedtakId asc, pe.periodeFom ")
  fun hentPerioderForStonadInkludertUgyldiggjorte(stonadId: Int): List<Periode>

  @Query(
    "select pe from Periode pe where pe.stonad.stonadId = :stonadId and pe.gyldigFra <= :gyldigTidspunkt and (pe.gyldigTil IS NULL or pe.gyldigTil >= :gyldigTidspunkt) order by pe.periodeGjortUgyldigAvVedtakId asc, pe.periodeFom ")
  fun hentGyldigePerioderForStonadForAngittTidspunkt(stonadId: Int, gyldigTidspunkt: LocalDateTime): List<Periode>

  @Query(
  "update Periode pe set pe.gyldigTil = :opprettetTidspunkt, pe.periodeGjortUgyldigAvVedtakId = :periodeGjortUgyldigAvVedtakId where pe.periodeId = :periodeId")
  @Modifying
  fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtakId: Int, opprettetTidspunkt: LocalDateTime)
}
