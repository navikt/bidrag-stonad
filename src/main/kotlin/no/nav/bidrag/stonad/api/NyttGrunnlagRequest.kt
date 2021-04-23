package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.GrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved et grunnlag")
data class NyttGrunnlagRequest(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
)

fun NyttGrunnlagRequest.toGrunnlagDto(stonadId: Int) = with(::GrunnlagDto) {
  val propertiesByName = NyttGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::stonadId.name -> stonadId
      GrunnlagDto::grunnlagId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}

fun NyttGrunnlagRequest.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = NyttGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::stonadId.name -> stonadId
      GrunnlagDto::grunnlagId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
