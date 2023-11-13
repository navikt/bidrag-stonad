package no.nav.bidrag.stønad.persistence.repository

import no.nav.bidrag.stønad.persistence.entity.Periode
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface PeriodeRepository : CrudRepository<Periode, Int?> {

    @Query(
        "select pe from Periode pe where pe.stønad.stønadsid = :stønadsid and pe.periodeGjortUgyldigAvVedtaksid IS NULL order by pe.fom",
    )
    fun hentPerioderForStonad(stønadsid: Int): List<Periode>

    @Query(
        "select pe from Periode pe where pe.stønad.stønadsid = :stønadsid order by pe.periodeGjortUgyldigAvVedtaksid asc, pe.fom ",
    )
    fun hentPerioderForStonadInkludertUgyldiggjorte(stønadsid: Int): List<Periode>

    @Query(
        "select pe from Periode pe where pe.stønad.stønadsid = :stønadsid and pe.gyldigFra <= :gyldigTidspunkt and (pe.gyldigTil IS NULL or pe.gyldigTil >= :gyldigTidspunkt) order by pe.fom",
    )
    fun hentGyldigePerioderForStonadForAngittTidspunkt(stønadsid: Int, gyldigTidspunkt: LocalDateTime): List<Periode>

    @Query(
        "update Periode pe set pe.gyldigTil = :vedtakstidspunkt, pe.periodeGjortUgyldigAvVedtaksid = :periodeGjortUgyldigAvVedtaksid where pe.periodeid = :periodeid",
    )
    @Modifying
    fun settPeriodeSomUgyldig(periodeid: Int, periodeGjortUgyldigAvVedtaksid: Int, vedtakstidspunkt: LocalDateTime)
}
