package no.nav.bidrag.stonad.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
    const val MOTTAKER_ID_HISTORIKK_SOK = "/mottakeridhistorikk/sok"
    private val LOGGER = LoggerFactory.getLogger(MottakerIdHistorikkController::class.java)
  }
}
