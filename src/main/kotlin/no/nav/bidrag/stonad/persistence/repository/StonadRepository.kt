package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Stonad
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface StonadRepository : CrudRepository<Stonad, Int?>{

  @Query(
    "select st from Stonad st where st.stonadType = :stonadType and st.skyldnerId = :skyldnerId and st.kravhaverId = :kravhaverId")
  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String): Stonad?

  @Query(
    "update Stonad st set st.endretAvSaksbehandlerId = :endretAv, st.endretTimestamp = CURRENT_TIMESTAMP where st.stonadId = :stonadId")
  @Modifying
  fun oppdaterStonad(stonadId: Int, endretAv: String)
}
