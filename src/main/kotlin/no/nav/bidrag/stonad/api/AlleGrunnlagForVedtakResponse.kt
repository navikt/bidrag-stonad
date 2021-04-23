package no.nav.bidrag.stonad.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.stonad.dto.GrunnlagDto

@ApiModel
data class AlleGrunnlagForstonadResponse(

  @ApiModelProperty(value = "Alle grunnlag for et stonad")
  val alleGrunnlagForstonad: List<GrunnlagDto> = emptyList()
)
