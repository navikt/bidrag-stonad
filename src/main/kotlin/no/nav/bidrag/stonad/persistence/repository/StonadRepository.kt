package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Stonad
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface StonadRepository : CrudRepository<Stonad, Int?>{

  @Query(
    "select st from Stonad st where st.stonadType = :stonadType and st.kravhaverId = :kravhaverId and st.skyldnerId = :skyldnerId"
  )
  fun hentStonad(stonadType: String, kravhaverId: String, skyldnerId: String): Stonad?

  @Query(
    "update Stonad st set st.endretAvSaksbehandlerId = :endretAv, st.endretTimestamp = :endretTimestamp " +
        "where st.stonadId = :stonadId "
  )
  fun oppdaterStonad(stonadId: Int, endretAv: String, endretTimestamp: LocalDateTime): Stonad
}
