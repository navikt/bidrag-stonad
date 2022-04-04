package no.nav.bidrag.stonad.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.StonadDto
import no.nav.bidrag.stonad.ISSUER
import no.nav.bidrag.stonad.service.StonadService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class StonadController(private val stonadService: StonadService) {

  @PostMapping(STONAD_NY)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter stønad")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Mottaker-id endret"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun opprettNyStonad(@RequestBody request: OpprettStonadRequestDto): ResponseEntity<Int>? {
    val stonadOpprettet = stonadService.opprettStonad(request)
    LOGGER.info("Stønad opprettet med stønadId: $stonadOpprettet")
    return ResponseEntity(stonadOpprettet, HttpStatus.OK)
  }


  @GetMapping(STONAD_HENT)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Stønad funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønad", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Stønad ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun hentStonad(@PathVariable stonadId: Int): ResponseEntity<StonadDto> {
    val stonadFunnet = stonadService.hentStonadFraId(stonadId)
    LOGGER.info("Følgende stønad ble funnet: $stonadFunnet")
    return ResponseEntity(stonadFunnet, HttpStatus.OK)
  }


  @PostMapping(STONAD_ENDRE_MOTTAKER_ID)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Endrer mottaker-id på en eksisterende stønad")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Mottaker-id endret"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun endreMottakerIdOgOpprettHistorikk(@RequestBody request: EndreMottakerIdRequestDto): ResponseEntity<Int> {
    val stonadIdEndretStonad = stonadService.endreMottakerIdOgOpprettHistorikk(request)
    LOGGER.info("Følgende forekomst på mottaker-id-historikk ble opprettet: $stonadIdEndretStonad")
    return ResponseEntity(stonadIdEndretStonad, HttpStatus.OK)
  }

  companion object {
    const val STONAD_NY = "/stonad"
    const val STONAD_HENT = "/stonad/{stonadId}"
    const val STONAD_ENDRE_MOTTAKER_ID = "/stonad/endre-mottaker-id"
    private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)
  }
}
