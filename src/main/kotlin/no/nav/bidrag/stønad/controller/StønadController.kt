package no.nav.bidrag.stønad.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.NotNull
import no.nav.bidrag.stønad.SECURE_LOGGER
import no.nav.bidrag.stønad.service.StønadService
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
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
@Timed
class StønadController(private val stønadService: StønadService) {

    @PostMapping(HENT_STØNAD)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en stønad")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Stønad funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønad", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Stønad ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentStonad(
        @NotNull @RequestBody
        request: HentStønadRequest,
    ): ResponseEntity<StønadDto> {
        val stønadFunnet = stønadService.hentStønad(request)
        LOGGER.info("Følgende stønadsid'er ble hentet: ${stønadFunnet?.stønadsid}")
        SECURE_LOGGER.info("Følgende stønad ble funnet: $stønadFunnet")
        return ResponseEntity(stønadFunnet, HttpStatus.OK)
    }

    @PostMapping(HENT_STØNAD_HISTORISK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en stønad for angitt tidspunkt")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Stønad funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønad", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Stønad ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentStonadHistorisk(
        @NotNull @RequestBody
        request: HentStønadHistoriskRequest,
    ): ResponseEntity<StønadDto> {
        val stønadFunnet = stønadService.hentStønadHistorisk(request)
        LOGGER.info("Følgende stønadsid ble hentet: ${stønadFunnet?.stønadsid}")
        SECURE_LOGGER.info("Følgende stønad med historiske verdier ble funnet: $stønadFunnet")
        return ResponseEntity(stønadFunnet, HttpStatus.OK)
    }

    @GetMapping(HENT_STØNADER_FOR_SAK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finner alle stønader innenfor angitt sak")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Sak funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønad", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Stønader ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentStønaderForSak(
        @PathVariable @NotNull
        sak: String,
    ): ResponseEntity<List<StønadDto>> {
        val stønaderFunnet = stønadService.hentStønaderForSak(sak)
        SECURE_LOGGER.info("Stønader ble hentet for sak: $sak)")
        SECURE_LOGGER.info("Følgende stønader ble funnet for sak: $stønaderFunnet")
        return ResponseEntity(stønaderFunnet, HttpStatus.OK)
    }

    companion object {
        const val HENT_STØNAD = "/hent-stonad"
        const val HENT_STØNAD_HISTORISK = "/hent-stonad-historisk"
        const val HENT_STØNADER_FOR_SAK = "/hent-stonader-for-sak/{sak}"
        private val LOGGER = LoggerFactory.getLogger(StønadController::class.java)
    }
}
