package no.nav.bidrag.stonad.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse
import no.nav.bidrag.stonad.api.EndreMottakerIdHistorikkRequest
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.service.MottakerIdHistorikkService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class MottakerIdHistorikkController(private val mottakerIdHistorikkService: MottakerIdHistorikkService) {

  @PostMapping(MOTTAKER_ID_HISTORIKK_NY)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Opprett ny forekomst på mottaker-id-historikk")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Forekomst på mottaker-id-historikk opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "501", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettMottakerIdHistorikk(@RequestBody request: EndreMottakerIdHistorikkRequest): ResponseEntity<MottakerIdHistorikkDto>? {
    val mottakerIdHistorikkOpprettet = mottakerIdHistorikkService.opprettNyMottakerIdHistorikk(request)
    LOGGER.info("Følgende forekomst på mottaker-id-historikk ble opprettet: $mottakerIdHistorikkOpprettet")
    return ResponseEntity(mottakerIdHistorikkOpprettet, HttpStatus.OK)
  }


  @GetMapping("$MOTTAKER_ID_HISTORIKK_SOK/{stonadId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle endringer i mottaker-id for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Endringer for stønad funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt stonad"),
      ApiResponse(responseCode = "404", description = "stonad ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun finnAlleEndringerMottakerIdForStonad(@PathVariable stonadId: Int): ResponseEntity<AlleMottakerIdHistorikkForStonadResponse> {
    val endringerFunnet = mottakerIdHistorikkService.finnAlleEndringerMottakerIdForStonad(stonadId)
    LOGGER.info("Følgende stonad ble funnet: $endringerFunnet")
    return ResponseEntity(endringerFunnet, HttpStatus.OK)
  }

  companion object {
    const val MOTTAKER_ID_HISTORIKK_NY  = "/mottakeridhistorikk/ny"
    const val MOTTAKER_ID_HISTORIKK_SOK = "/mottakeridhistorikk/sok"
    private val LOGGER = LoggerFactory.getLogger(MottakerIdHistorikkController::class.java)
  }
}
