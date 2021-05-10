package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.MottakerIdHistorikk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MottakerIdHistorikkRepository : JpaRepository<MottakerIdHistorikk, Int?>{

  @Query(
    "select mh from MottakerIdHistorikk mh where mh.stonad.stonadId = :stonadId")
  fun hentAlleMottakerIdHistorikkForStonad(stonadId: Int): List<MottakerIdHistorikk>
}
