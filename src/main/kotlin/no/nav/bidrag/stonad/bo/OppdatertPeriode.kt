package no.nav.bidrag.stonad.bo


data class OppdatertPeriode(
  val periodeListe: List<PeriodeBo>,
  val oppdaterPerioder: Boolean = false,
  val settPeriodeSomUgyldig: Boolean = false
)