package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.Grunnlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GrunnlagRepository : JpaRepository<Grunnlag, Int?>{

  @Query(
    "select gr from Grunnlag gr where gr.stonad.stonadId = :stonadId")
  fun hentAlleGrunnlagForstonad(stonadId: Int): List<Grunnlag>
}
