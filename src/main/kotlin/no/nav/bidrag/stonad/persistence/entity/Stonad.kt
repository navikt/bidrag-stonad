package no.nav.bidrag.stonad.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import no.nav.bidrag.domain.enums.Innkreving
import no.nav.bidrag.domain.enums.StonadType
import no.nav.bidrag.transport.behandling.stonad.response.StonadDto
import no.nav.bidrag.transport.behandling.stonad.response.StonadPeriodeDto
import no.nav.bidrag.transport.behandling.stonad.request.OpprettStonadRequestDto
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@Entity
data class Stonad(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stonad_id")
    val stonadId: Int = 0,

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "sak_id")
    val sakId: String = "",

    @Column(nullable = false, name = "skyldner_id")
    val skyldnerId: String = "",

    @Column(nullable = false, name = "kravhaver_id")
    val kravhaverId: String = "",

    @Column(nullable = false, name = "mottaker_id")
    val mottakerId: String = "",

    @Column(nullable = true, name = "indeksregulering_aar")
    val indeksreguleringAar: String? = "",

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "opprettet_av")
    val opprettetAv: String = "",

    @Column(nullable = false, name = "opprettet_tidspunkt")
    val opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true, name = "endret_av")
    val endretAv: String? = null,

    @Column(nullable = true, name = "endret_tidspunkt")
    val endretTidspunkt: LocalDateTime? = null
)

fun OpprettStonadRequestDto.toStonadEntity() = with(::Stonad) {
    val propertiesByName = OpprettStonadRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Stonad::stonadId.name -> 0
                Stonad::type.name -> type.toString()
                Stonad::innkreving.name -> innkreving.toString()
                Stonad::opprettetTidspunkt.name -> LocalDateTime.now()
                Stonad::endretAv.name -> opprettetAv
                else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
            }
        }
    )
}

fun Stonad.toStonadDto(stonadPeriodeDtoListe: List<StonadPeriodeDto>) = with(::StonadDto) {
    val propertiesByName = Stonad::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                StonadDto::type.name -> StonadType.valueOf(type)
                StonadDto::innkreving.name -> Innkreving.valueOf(innkreving)
                StonadDto::periodeListe.name -> stonadPeriodeDtoListe
                else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
            }
        }
    )
}
