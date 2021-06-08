package no.nav.bidrag.stonad.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
  @ApiOperation("Opprett ny forekomst på mottaker-id-historikk")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Forekomst på mottaker-id-historikk opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 501, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettMottakerIdHistorikk(@RequestBody request: EndreMottakerIdHistorikkRequest): ResponseEntity<MottakerIdHistorikkDto>? {
    val mottakerIdHistorikkOpprettet = mottakerIdHistorikkService.opprettNyMottakerIdHistorikk(request)
    LOGGER.info("Følgende forekomst på mottaker-id-historikk ble opprettet: $mottakerIdHistorikkOpprettet")
    return ResponseEntity(mottakerIdHistorikkOpprettet, HttpStatus.OK)
  }


  @GetMapping("$MOTTAKER_ID_HISTORIKK_SOK/{stonadId}")
  @ApiOperation("Finn alle endringer i mottaker-id for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Endringer for stønad funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt stonad"),
      ApiResponse(code = 404, message = "stonad ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
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
