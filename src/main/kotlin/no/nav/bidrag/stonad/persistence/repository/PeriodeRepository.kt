package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Periode
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?>{

  @Query(
    "select pe from Periode pe where pe.stonadsendring.stonadsendringId = :stonadsendringsId")
  fun hentAllePerioderForStonadsendring(stonadsendringsId: Int): List<Periode>
}
