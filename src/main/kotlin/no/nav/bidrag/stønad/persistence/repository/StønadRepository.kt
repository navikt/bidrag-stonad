package no.nav.bidrag.stønad.persistence.repository

import no.nav.bidrag.stønad.persistence.entity.Stønad
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StønadRepository : CrudRepository<Stønad, Int?> {

    @Query(
        "select st from Stønad st where st.type = :stønadstype and st.skyldner = :skyldner and st.kravhaver = :kravhaver and st.sak = :sak",
    )
    fun finnStønad(stønadstype: String, skyldner: String, kravhaver: String, sak: String): Stønad?

    @Query(
        "update Stønad st set st.endretAv = :opprettetAv, st.endretTidspunkt = CURRENT_TIMESTAMP where st.stønadsid = :stønadsid",
    )
    @Modifying
    fun oppdaterStonadMedEndretAvOgTimestamp(stønadsid: Int, opprettetAv: String)

    @Query(
        "update Stønad st set st.mottaker = :mottaker, st.endretAv = :opprettetAv, st.endretTidspunkt = CURRENT_TIMESTAMP where st.stønadsid = :stønadsid",
    )
    @Modifying
    fun endreMottakerForStønad(stønadsid: Int, mottaker: String, opprettetAv: String)

    @Query(
        "select st from Stønad st where st.sak = :sak order by st.stønadsid",
    )
    fun finnStønaderForSak(sak: String): List<Stønad>
}
