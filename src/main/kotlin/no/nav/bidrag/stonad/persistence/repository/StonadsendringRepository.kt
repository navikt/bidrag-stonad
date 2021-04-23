package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Stonadsendring
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StonadsendringRepository : CrudRepository<Stonadsendring, Int?>{

  @Query(
    "select st from Stonadsendring st where st.stonad.stonadId = :stonadId")
  fun hentAlleStonadsendringerForstonad(stonadId: Int): List<Stonadsendring>
}
