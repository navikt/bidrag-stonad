package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragstonadLocal
import no.nav.bidrag.stonad.TestUtil.Companion.byggKomplettstonadRequest
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.dto.stonadDto
import no.nav.bidrag.stonad.persistence.repository.GrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadsendringRepository
import no.nav.bidrag.stonad.persistence.repository.stonadRepository
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
@ActiveProfiles(BidragstonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragstonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class stonadServiceTest {

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var stonadRepository: stonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt stonad`() {
    // Oppretter nytt stonad
    val nyttstonadRequest = NyttstonadRequest("TEST", "1111")
    val nyttstonadOpprettet = stonadService.opprettNyttstonad(nyttstonadRequest)

    assertAll(
      Executable { assertThat(nyttstonadOpprettet).isNotNull() },
      Executable { assertThat(nyttstonadOpprettet.saksbehandlerId).isEqualTo(nyttstonadRequest.saksbehandlerId) },
      Executable { assertThat(nyttstonadOpprettet.enhetId).isEqualTo(nyttstonadRequest.enhetId) }
    )
  }

  @Test
  fun `skal finne data for ett stonad`() {
    // Oppretter nytt stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Finner stonadet som akkurat ble opprettet
    val stonadFunnet = stonadService.finnEttstonad(nyttstonadOpprettet.stonadId)

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
      Executable { assertThat(stonadFunnet.stonadId).isEqualTo(nyttstonadOpprettet.stonadId) },
      Executable { assertThat(stonadFunnet.saksbehandlerId).isEqualTo(nyttstonadOpprettet.saksbehandlerId) },
      Executable { assertThat(stonadFunnet.enhetId).isEqualTo(nyttstonadOpprettet.enhetId) }
    )
  }

  @Test
  fun `skal finne data for alle stonad`() {
    // Oppretter nye stonad
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "2222"))

    // Finner begge stonadene som akkurat ble opprettet
    val stonadFunnet = stonadService.finnAllestonad()

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
      Executable { assertThat(stonadFunnet.allestonad).isNotNull() },
      Executable { assertThat(stonadFunnet.allestonad.size).isEqualTo(2) },
      Executable { assertThat(stonadFunnet.allestonad[0]).isNotNull() },
      Executable { assertThat(stonadFunnet.allestonad[0].stonadId).isEqualTo(nyttstonadOpprettet1.stonadId) },
      Executable { assertThat(stonadFunnet.allestonad[0].saksbehandlerId).isEqualTo(nyttstonadOpprettet1.saksbehandlerId) },
      Executable { assertThat(stonadFunnet.allestonad[0].enhetId).isEqualTo(nyttstonadOpprettet1.enhetId) },
      Executable { assertThat(stonadFunnet.allestonad[1]).isNotNull() },
      Executable { assertThat(stonadFunnet.allestonad[1].stonadId).isEqualTo(nyttstonadOpprettet2.stonadId) },
      Executable { assertThat(stonadFunnet.allestonad[1].saksbehandlerId).isEqualTo(nyttstonadOpprettet2.saksbehandlerId) },
      Executable { assertThat(stonadFunnet.allestonad[1].enhetId).isEqualTo(nyttstonadOpprettet2.enhetId) }
    )
  }


  @Test
  fun `skal opprette nytt komplett stonad`() {
    // Oppretter nytt komplett stonad
    val nyttKomplettstonadRequest = byggKomplettstonadRequest()
    val nyttKomplettstonadOpprettet = stonadService.opprettKomplettstonad(nyttKomplettstonadRequest)

    assertAll(
      Executable { assertThat(nyttKomplettstonadOpprettet).isNotNull() },
      Executable { assertThat(nyttKomplettstonadOpprettet.stonadId).isNotNull() }
    )
  }
}
