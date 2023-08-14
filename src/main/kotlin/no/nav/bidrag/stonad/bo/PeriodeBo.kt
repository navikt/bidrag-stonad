package no.nav.bidrag.stonad.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.stonad.persistence.entity.Periode
import no.nav.bidrag.stonad.persistence.entity.Stonad
import no.nav.bidrag.transport.behandling.stonad.reponse.StonadPeriodeDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStonadPeriodeRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class PeriodeBo(

    @Schema(description = "Periode-id")
    val periodeId: Int = 0,

    @Schema(description = "Periode fra-og-med-dato")
    val periodeFom: LocalDate = LocalDate.now(),

    @Schema(description = "Periode til-dato")
    val periodeTil: LocalDate? = null,

    @Schema(description = "Stonad-id")
    val stonadId: Int = 0,

    @Schema(description = "Vedtak-id")
    val vedtakId: Int = 0,

    @Schema(description = "Perioden er gyldig fra angitt tidspunkt (vedtakstidspunkt)")
    val gyldigFra: LocalDateTime = LocalDateTime.now(),

    @Schema(description = "Angir tidspunkt perioden eventuelt er ugyldig fra (tidspunkt for vedtak med periode som erstattet denne)")
    val gyldigTil: LocalDateTime? = null,

    @Schema(description = "Periode-gjort-ugyldig-av-vedtak-id")
    val periodeGjortUgyldigAvVedtakId: Int? = 0,

    @Schema(description = "Beregnet stønadsbeløp")
    val belop: BigDecimal? = BigDecimal.ZERO,

    @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
    val valutakode: String? = "NOK",

    @Schema(description = "Resultatkode for stønaden")
    val resultatkode: String = ""
)

fun OpprettStonadPeriodeRequestDto.toPeriodeBo() = with(::PeriodeBo) {
    val propertiesByName = OpprettStonadPeriodeRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                PeriodeBo::periodeId.name -> 0
                PeriodeBo::stonadId.name -> 0
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
            }
        }
    )
}

fun StonadPeriodeDto.toPeriodeBo() = with(::PeriodeBo) {
    val propertiesByName = StonadPeriodeDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                PeriodeBo::stonadId.name -> stonadId
                PeriodeBo::periodeId.name -> 0
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeBo)
            }
        }
    )
}

fun PeriodeBo.toPeriodeEntity(eksisterendeStonad: Stonad) = with(::Periode) {
    val propertiesByName = PeriodeBo::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Periode::stonad.name -> eksisterendeStonad
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
            }
        }
    )
}

fun PeriodeBo.toJustertPeriodeEntity(eksisterendeStonad: Stonad, vedtakTidspunkt: LocalDateTime) = with(::Periode) {
    val propertiesByName = PeriodeBo::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Periode::stonad.name -> eksisterendeStonad
                Periode::gyldigFra.name -> vedtakTidspunkt
                else -> propertiesByName[parameter.name]?.get(this@toJustertPeriodeEntity)
            }
        }
    )
}
