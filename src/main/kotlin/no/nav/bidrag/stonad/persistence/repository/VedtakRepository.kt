package no.nav.bidrag.stonad.persistence.repository

import no.nav.bidrag.stonad.persistence.entity.stonad
import org.springframework.data.jpa.repository.JpaRepository

interface stonadRepository : JpaRepository<stonad, Int?>
