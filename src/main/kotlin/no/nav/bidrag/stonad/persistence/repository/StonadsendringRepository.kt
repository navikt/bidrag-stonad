package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Stonad
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StonadsendringRepository : CrudRepository<Stonad, Int?>{

  @Query(
    "select st from Stonad st where st.stonad.stonadId = :stonadId"
  )
  fun hentAlleStonadsendringerForstonad(stonadId: Int): List<Stonad>
}
