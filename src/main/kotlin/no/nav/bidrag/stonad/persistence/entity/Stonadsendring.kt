package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.StonadsendringDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Stonadsendring(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonadsendring_id")
  val stonadsendringId: Int = 0,

  @Column(nullable = false, name = "stonad_type")
  val stonadType: String = "",

  @ManyToOne
  @JoinColumn(name = "stonad_id")
  val stonad: stonad = stonad(),

  @Column(nullable = true, name = "sak_id")
  val sakId: String? = null,

  @Column(nullable = true, name = "behandling_id")
  val behandlingId: String? = null,

  @Column(nullable = false, name = "skyldner_id")
  val skyldnerId: String = "",

  @Column(nullable = false, name = "kravhaver_id")
  val kravhaverId: String = "",

  @Column(nullable = false, name = "mottaker_id")
  val mottakerId: String = ""
)

fun Stonadsendring.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = Stonadsendring::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::stonadId.name -> stonad.stonadId
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}