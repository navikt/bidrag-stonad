package no.nav.bidrag.stonad.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.NotNull
import no.nav.bidrag.stonad.SECURE_LOGGER
import no.nav.bidrag.stonad.service.StonadService
import no.nav.bidrag.transport.behandling.stonad.response.StonadDto
import no.nav.bidrag.transport.behandling.stonad.request.HentStonadHistoriskRequest
import no.nav.bidrag.transport.behandling.stonad.request.HentStonadRequest
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
class StonadController(private val stonadService: StonadService) {

    @PostMapping(HENT_STONAD)
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
    fun hentStonad(
        @NotNull @RequestBody
        request: HentStonadRequest
    ): ResponseEntity<StonadDto> {
        val stonadFunnet = stonadService.hentStonad(request)
        LOGGER.info("Følgende stønad-id ble hentet: ${stonadFunnet?.stonadId}")
        SECURE_LOGGER.info("Følgende stønad ble funnet: $stonadFunnet")
        return ResponseEntity(stonadFunnet, HttpStatus.OK)
    }

    @PostMapping(HENT_STONAD_HISTORISK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en stønad for angitt tidspunkt")
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
    fun hentStonadHistorisk(
        @NotNull @RequestBody
        request: HentStonadHistoriskRequest
    ): ResponseEntity<StonadDto> {
        val stonadFunnet = stonadService.hentStonadHistorisk(request)
        LOGGER.info("Følgende stønad-id ble hentet: ${stonadFunnet?.stonadId}")
        SECURE_LOGGER.info("Følgende stønad med historiske verdier ble funnet: $stonadFunnet")
        return ResponseEntity(stonadFunnet, HttpStatus.OK)
    }

    @GetMapping(HENT_STONADER_FOR_SAKID)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finner alle stønader innenfor angitt sakId")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "SakId funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønad", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Stønader ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
        ]
    )
    fun hentStonaderForSakId(
        @PathVariable @NotNull
        sakId: String
    ): ResponseEntity<List<StonadDto>> {
        val stonaderFunnet = stonadService.hentStonaderForSakId(sakId)
        SECURE_LOGGER.info("Stønader ble hentet for sakId: $sakId")
        SECURE_LOGGER.info("Følgende stønader ble funnet for sakId: $stonaderFunnet")
        return ResponseEntity(stonaderFunnet, HttpStatus.OK)
    }

    companion object {
        const val HENT_STONAD = "/hent-stonad"
        const val HENT_STONAD_HISTORISK = "/hent-stonad-historisk"
        const val HENT_STONADER_FOR_SAKID = "/hent-stonader-for-sakid/{sakId}"
        private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)
    }
}
