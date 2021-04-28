package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Endrer mottaker-id på en stønad")
data class NyEndreMottakerIdRequest(

  @ApiModelProperty(value = "Referanse til stonaden")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Verdi for Mottaker-id som skal endres")
  val mottakerIdEndretFra: String = "",

  @ApiModelProperty(value = "Verdi for Mottaker-id som det skal endres til")
  val mottakerIdEndretTil: String = "",

)

/*fun NyEndreMottakerIdRequest.toGrunnlagDto(stonadId: Int) = with(::GrunnlagDto) {
  val propertiesByName = EndreMottakerIdRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::stonadId.name -> stonadId
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}

fun NyEndreMottakerIdRequest.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = EndreMottakerIdRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::stonadId.name -> stonadId
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
*/
