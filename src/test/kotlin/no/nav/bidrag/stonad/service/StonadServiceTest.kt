package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.api.NyStonadRequest
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("stonadServiceTest")
@ActiveProfiles(BidragStonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StonadServiceTest {

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny stonad`() {
    // Oppretter nytt stonad
    val nyStonadRequest = NyStonadRequest("TEST", "1111")
    val nyStonadOpprettet = stonadService.opprettStonad(nyStonadRequest)

    assertAll(
      Executable { assertThat(nyStonadOpprettet).isNotNull() },
    )
  }

  @Test
  fun `skal finne alle data for en stonad`() {
    // Oppretter nytt stonad
    val nyStonadOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "Test",
      opprettetAvSaksbehandlerId = "111"
    ))

    // Finner stønaden som akkurat ble opprettet
    val stonadFunnet = stonadService.finnStonad(nyStonadOpprettet.stonadId)

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
    )
  }


}
