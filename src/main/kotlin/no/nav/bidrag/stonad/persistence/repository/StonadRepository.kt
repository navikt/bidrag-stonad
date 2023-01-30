package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Stonad
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StonadRepository : CrudRepository<Stonad, Int?>{

  @Query(
    "select st from Stonad st where st.type = :stonadType and st.skyldnerId = :skyldnerId and st.kravhaverId = :kravhaverId and st.sakId = :sakId"
  )
  fun finnStonad(stonadType: String, skyldnerId: String, kravhaverId: String, sakId: String): Stonad?

  @Query(
    "update Stonad st set st.endretAv = :opprettetAv, st.endretTidspunkt = CURRENT_TIMESTAMP where st.stonadId = :stonadId"
  )
  @Modifying
  fun oppdaterStonadMedEndretAvOgTimestamp(stonadId: Int, opprettetAv: String)

  @Query(
    "update Stonad st set st.mottakerId = :mottakerId, st.endretAv = :opprettetAv, st.endretTidspunkt = CURRENT_TIMESTAMP where st.stonadId = :stonadId"
  )
  @Modifying
  fun endreMottakerIdForStonad(stonadId: Int, mottakerId: String, opprettetAv: String)
}
