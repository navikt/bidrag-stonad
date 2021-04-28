/*
package no.nav.bidrag.stonad.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.api.NyKomplettStonadRequest
import no.nav.bidrag.stonad.api.NyStonadResponse
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.service.StonadService
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
class MottakerIdHistorikkController(private val stonadService: StonadService) {

  @PostMapping(stonad_NY)
  @ApiOperation("Opprett nytt stonad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "stonad opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettNyttstonad(@RequestBody request: NyttstonadRequest): ResponseEntity<MottakerIdHistorikkDto>? {
    val stonadOpprettet = stonadService.opprettNystonad(request)
    LOGGER.info("Følgende stonad er opprettet: $stonadOpprettet")
    return ResponseEntity(stonadOpprettet, HttpStatus.OK)
  }

  @GetMapping("$stonad_SOK/{stonadId}")
  @ApiOperation("Finn data for ett stonad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "stonad funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt stonad"),
      ApiResponse(code = 404, message = "stonad ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnEttstonad(@PathVariable stonadId: Int): ResponseEntity<MottakerIdHistorikkDto> {
    val stonadFunnet = stonadService.finnEttstonad(stonadId)
    LOGGER.info("Følgende stonad ble funnet: $stonadFunnet")
    return ResponseEntity(stonadFunnet, HttpStatus.OK)
  }

  @GetMapping(stonad_SOK)
  @ApiOperation("Finn data for alle stonad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle stonad funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnAllestonad(): ResponseEntity<no.nav.bidrag.stonad.api.AllestonadResponse> {
    val allestonad = stonadService.finnAllestonad()
    LOGGER.info("Alle stonad ble funnet")
    return ResponseEntity(allestonad, HttpStatus.OK)
  }

  @PostMapping(stonad_NY_KOMPLETT)
  @ApiOperation("Opprett nytt komplett stonad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Komplett stonad opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettKomplettstonad(@RequestBody request: NyKomplettStonadRequest): ResponseEntity<NyStonadResponse>? {
    val stonadOpprettet = stonadService.opprettKomplettstonad(request)
    LOGGER.info("stonad med id ${stonadOpprettet.stonadId} er opprettet")
    return ResponseEntity(stonadOpprettet, HttpStatus.OK)
  }

  companion object {
    const val stonad_SOK = "/stonad"
    const val stonad_NY = "/stonad/ny"
    const val stonad_NY_KOMPLETT = "/stonad/ny/komplett"
    private val LOGGER = LoggerFactory.getLogger(MottakerIdHistorikkController::class.java)
  }
}
*/
