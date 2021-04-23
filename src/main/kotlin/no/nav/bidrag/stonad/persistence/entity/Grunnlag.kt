package no.nav.bidrag.stonad.persistence.entity

import no.nav.bidrag.stonad.dto.GrunnlagDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Grunnlag(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "grunnlag_id")
  val grunnlagId: Int = 0,

  @Column(nullable = false, name = "grunnlag_referanse")
  val grunnlagReferanse: String = "",

  @ManyToOne
  @JoinColumn(name = "stonad_id")
  val stonad: stonad = stonad(),

  @Column(nullable = false, name = "grunnlag_type")
  val grunnlagType: String = "",

  @Column(nullable = false, name = "grunnlag_innhold")
  val grunnlagInnhold: String = "",

  )

fun Grunnlag.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = Grunnlag::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::stonadId.name -> stonad.stonadId
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
