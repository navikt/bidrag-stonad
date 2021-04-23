package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.api.AllestonadResponse
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.api.NyttGrunnlagRequest
import no.nav.bidrag.stonad.api.NyPeriodeRequest
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.NyttKomplettstonadRequest
import no.nav.bidrag.stonad.api.NyttstonadResponse
import no.nav.bidrag.stonad.api.toGrunnlagDto
import no.nav.bidrag.stonad.api.toPeriodeDto
import no.nav.bidrag.stonad.api.toStonadsendringDto
import no.nav.bidrag.stonad.controller.PeriodeController
import no.nav.bidrag.stonad.dto.GrunnlagDto
import no.nav.bidrag.stonad.dto.PeriodeGrunnlagDto
import no.nav.bidrag.stonad.dto.stonadDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadService (val persistenceService: PersistenceService) {

  private val grunnlagIdRefMap = mutableMapOf<String, Int>()
  private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)

  fun opprettNyttstonad(request: NyttstonadRequest): stonadDto {
    val stonadDto = stonadDto(enhetId = request.enhetId, saksbehandlerId = request.saksbehandlerId)
    return persistenceService.opprettNyttstonad(stonadDto)
  }

  fun finnEttstonad(stonad_id: Int): stonadDto {
    return persistenceService.finnEttstonad(stonad_id)
  }

  fun finnAllestonad(): no.nav.bidrag.stonad.api.AllestonadResponse {
    return no.nav.bidrag.stonad.api.AllestonadResponse(persistenceService.finnAllestonad())
  }

  // Opprett komplett stonad (alle tabeller)
  fun opprettKomplettstonad(stonadRequest: NyttKomplettstonadRequest): NyttstonadResponse {

    // Opprett stonad
    val stonadDto = stonadDto(enhetId = stonadRequest.enhetId, saksbehandlerId = stonadRequest.saksbehandlerId)
    val opprettetstonad = persistenceService.opprettNyttstonad(stonadDto)

    // Grunnlag
    stonadRequest.grunnlagListe.forEach {
      val opprettetGrunnlag = opprettGrunnlag(it, opprettetstonad.stonadId)
      grunnlagIdRefMap[it.grunnlagReferanse] = opprettetGrunnlag.grunnlagId
    }

    // Stønadsendring
    stonadRequest.stonadsendringListe.forEach { opprettStonadsendring(it, opprettetstonad.stonadId) }

    return NyttstonadResponse(opprettetstonad.stonadId)
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: NyttGrunnlagRequest, stonadId: Int): GrunnlagDto {
    return persistenceService.opprettNyttGrunnlag(grunnlagRequest.toGrunnlagDto(stonadId))
  }

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: NyStonadsendringRequest, stonadId: Int) {
    val opprettetStonadsendring = persistenceService.opprettNyStonadsendring(stonadsendringRequest.toStonadsendringDto(stonadId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId) }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: NyPeriodeRequest, stonadsendringId: Int) {
    val opprettetPeriode = persistenceService.opprettNyPeriode(periodeRequest.toPeriodeDto(stonadsendringId))

    // PeriodeGrunnlag
    periodeRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it.grunnlagReferanse, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse ${it.grunnlagReferanse} ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val periodeGrunnlagDto = PeriodeGrunnlagDto(periodeId = opprettetPeriode.periodeId, grunnlagId = grunnlagId, grunnlagValgt = it.grunnlagValgt)
        persistenceService.opprettNyttPeriodeGrunnlag(periodeGrunnlagDto)
      }
    }
  }
}
