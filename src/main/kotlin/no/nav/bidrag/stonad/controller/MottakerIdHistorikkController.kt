package no.nav.bidrag.stonad.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.ISSUER
import no.nav.bidrag.stonad.api.AlleMottakerIdHistorikkForStonadResponse
import no.nav.bidrag.stonad.service.MottakerIdHistorikkService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class MottakerIdHistorikkController(private val mottakerIdHistorikkService: MottakerIdHistorikkService) {

  @GetMapping("$MOTTAKER_ID_HISTORIKK_SOK/{stonadId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle endringer av mottaker-id for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Endringer for stønad funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt stonad", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "stonad ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun finnAlleEndringerMottakerIdForStonad(@PathVariable stonadId: Int): ResponseEntity<AlleMottakerIdHistorikkForStonadResponse> {
    val endringerFunnet = mottakerIdHistorikkService.finnAlleEndringerMottakerIdForStonad(stonadId)
    LOGGER.info("Følgende stonad ble funnet: $endringerFunnet")
    return ResponseEntity(endringerFunnet, HttpStatus.OK)
  }

  companion object {
    const val MOTTAKER_ID_HISTORIKK_SOK = "/mottakeridhistorikk/sok"
    private val LOGGER = LoggerFactory.getLogger(MottakerIdHistorikkController::class.java)
  }
}
