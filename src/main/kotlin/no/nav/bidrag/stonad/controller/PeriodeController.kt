package no.nav.bidrag.stonad.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.stonad.dto.PeriodeDto
import no.nav.bidrag.stonad.service.PeriodeService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class PeriodeController(private val periodeService: PeriodeService) {

  @GetMapping("$PERIODE_SOK/{periodeId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn data for en periode")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Data for periode hentet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(responseCode = "404", description = "Periode ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )
  fun finnPeriode(@PathVariable periodeId: Int): ResponseEntity<PeriodeDto> {
    val periodeFunnet = periodeService.finnPeriode(periodeId)
    LOGGER.info("Følgende periode ble funnet: $periodeFunnet")
    return ResponseEntity(periodeFunnet, HttpStatus.OK)
  }


  companion object {
    const val PERIODE_SOK = "/periode"
    const val PERIODE_NY = "/periode/ny"
    private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)
  }
}
