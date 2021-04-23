package no.nav.bidrag.stonad.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.persistence.entity.Grunnlag
import no.nav.bidrag.stonad.persistence.entity.stonad
import kotlin.reflect.full.memberProperties

data class GrunnlagDto(

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
)

fun GrunnlagDto.toGrunnlagEntity(eksisterendestonad: stonad) = with(::Grunnlag) {
  val propertiesByName = GrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Grunnlag::stonad.name -> eksisterendestonad
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
    }
  })
}
