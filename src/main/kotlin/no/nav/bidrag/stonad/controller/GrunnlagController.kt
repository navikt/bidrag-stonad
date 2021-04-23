package no.nav.bidrag.stonad.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.stonad.dto.GrunnlagDto
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class GrunnlagController(private val grunnlagService: GrunnlagService) {


  @GetMapping("$GRUNNLAG_SOK/{grunnlagId}")
  @ApiOperation("Finn data for et grunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Grunnlag funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnGrunnlag(@PathVariable grunnlagId: Int): ResponseEntity<GrunnlagDto> {
    val grunnlagFunnet = grunnlagService.finnGrunnlag(grunnlagId)
    LOGGER.info("Følgende grunnlag ble funnet: $grunnlagFunnet")
    return ResponseEntity(grunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$GRUNNLAG_SOK_stonad/{stonadId}")
  @ApiOperation("finner alle grunnlag for et stonad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle grunnlag funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet for stonad"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun finnGrunnlagForstonad(@PathVariable stonadId: Int):
      ResponseEntity<no.nav.bidrag.stonad.api.AlleGrunnlagForstonadResponse> {
    val alleGrunnlagFunnet = grunnlagService.finnAlleGrunnlagForstonad(stonadId)
    LOGGER.info("Følgende grunnlag ble funnet: $alleGrunnlagFunnet")
    return ResponseEntity(alleGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val GRUNNLAG_SOK = "/grunnlag"
    const val GRUNNLAG_SOK_stonad = "/grunnlag/stonad"
    private val LOGGER = LoggerFactory.getLogger(GrunnlagController::class.java)
  }
}
