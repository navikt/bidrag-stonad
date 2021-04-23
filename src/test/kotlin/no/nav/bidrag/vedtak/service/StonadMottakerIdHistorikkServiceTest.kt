package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.api.NyStonadsendringRequest
import no.nav.bidrag.stonad.api.NyttstonadRequest
import no.nav.bidrag.stonad.dto.StonadsendringDto
import no.nav.bidrag.stonad.dto.stonadDto
import no.nav.bidrag.stonad.persistence.repository.GrunnlagRepository
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

@DisplayName("StonadsendringServiceTest")
@ActiveProfiles(BidragStonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StonadMottakerIdHistorikkServiceTest {

  @Autowired
  private lateinit var stonadsendringService: StonadMottakerIdHistorikkService

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var stonadRepository: stonadRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny stonadsendring`() {
    // Oppretter nytt stonad
    val nyttstonadRequest = NyttstonadRequest("TEST", "1111")
    val nyttstonadOpprettet = stonadService.opprettNyttstonad(nyttstonadRequest)

    // Oppretter ny stønadsendring
    val nyStonadsendringRequest = NyStonadsendringRequest(
      "BIDRAG",
      nyttstonadOpprettet.stonadId,
      "1111",
      "1111",
      "1111",
      "1111"
    )
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    assertAll(
      Executable { assertThat(nyStonadsendringOpprettet).isNotNull() },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo(nyStonadsendringRequest.stonadType) },
      Executable { assertThat(nyStonadsendringOpprettet.stonadId).isEqualTo(nyStonadsendringRequest.stonadId) },
      Executable { assertThat(nyStonadsendringOpprettet.behandlingId).isEqualTo(nyStonadsendringRequest.behandlingId) }
    )
  }

  @Test
  fun `skal finne data for en stonadsendring`() {
    // Oppretter nytt stonad
    val nyttstonadOpprettet = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        stonadId = nyttstonadOpprettet.stonadId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    // Finner stønadsendringen som akkurat ble opprettet
    val stonadsendringFunnet = stonadsendringService.finnEnStonadsendring(nyStonadsendringOpprettet.stonadsendringId)

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.stonadsendringId).isEqualTo(nyStonadsendringOpprettet.stonadsendringId) },
      Executable { assertThat(stonadsendringFunnet.stonadType).isEqualTo(nyStonadsendringOpprettet.stonadType) },
      Executable { assertThat(stonadsendringFunnet.stonadId).isEqualTo(nyStonadsendringOpprettet.stonadId) },
      Executable { assertThat(stonadsendringFunnet.behandlingId).isEqualTo(nyStonadsendringOpprettet.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }

  @Test
  fun `skal finne alle stonadsendringer for et stonad`() {

    // Oppretter nytt stonad
    val nyttstonadOpprettet1 = persistenceService.opprettNyttstonad(stonadDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttstonadOpprettet2 = persistenceService.opprettNyttstonad(stonadDto(17, saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye stønadsendringer
    val nyStonadsendringDtoListe = mutableListOf<StonadsendringDto>()

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          stonadId = nyttstonadOpprettet1.stonadId,
          behandlingId = "1111",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111"
        )
      )
    )

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          stonadId = nyttstonadOpprettet1.stonadId,
          behandlingId = "2222",
          skyldnerId = "2222",
          kravhaverId = "2222",
          mottakerId = "2222"
        )
      )
    )

    // Legger til en ekstra stonadsendring som ikke skal bli funnet pga annen stonadId
    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          stonadId = nyttstonadOpprettet2.stonadId,
          behandlingId = "9999",
          skyldnerId = "9999",
          kravhaverId = "9999",
          mottakerId = "9999"
        )
      )
    )

    // Finner begge stønadsendringene som akkurat ble opprettet
    val stonadId = nyttstonadOpprettet1.stonadId
    val stonadsendringFunnet = stonadsendringService.finnAlleStonadsendringerForstonad(stonadId)

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringerForstonad).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringerForstonad.size).isEqualTo(2) },
      Executable {
        stonadsendringFunnet.alleStonadsendringerForstonad.forEachIndexed { index, stonadsendring ->
          assertAll(
            Executable { assertThat(stonadsendring.stonadsendringId).isEqualTo(nyStonadsendringDtoListe[index].stonadsendringId) },
            Executable { assertThat(stonadsendring.stonadType).isEqualTo(nyStonadsendringDtoListe[index].stonadType) },
            Executable { assertThat(stonadsendring.stonadId).isEqualTo(nyStonadsendringDtoListe[index].stonadId) },
            Executable { assertThat(stonadsendring.behandlingId).isEqualTo(nyStonadsendringDtoListe[index].behandlingId) }
          )
        }
      }
    )
    stonadsendringRepository.deleteAll()
    stonadRepository.deleteAll()
  }
}
