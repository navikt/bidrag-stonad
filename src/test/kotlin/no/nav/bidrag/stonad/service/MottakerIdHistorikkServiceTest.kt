package no.nav.bidrag.stonad.service

import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.api.EndreMottakerIdRequest
import no.nav.bidrag.stonad.dto.StonadDto
import no.nav.bidrag.stonad.dto.MottakerIdHistorikkDto
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
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

@DisplayName("MottakerIdHistorikkServiceTest")
@ActiveProfiles(BidragStonadLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MottakerIdHistorikkServiceTest {

  @Autowired
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var mottakerIdHistorikkRepository: MottakerIdHistorikkRepository

  @Autowired
  private lateinit var mottakerIdHistorikkService: MottakerIdHistorikkService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }
/*
  @Test
  fun `skal opprette ny mottakerIdHistorikk`() {
    // Oppretter ny MottakerIdHistorikk
    val nyStonadRequest = NyStonadRequest("BIDRAG", "SAK-001", "01018011111", "01010511111",
      "01018211111", "X123456", "X654321")
    val nyStonadOpprettet = stonadService.opprettStonad(nyStonadRequest)

    // Oppretter ny MottakerIdHistorikk
    val nyMottakerIdHistorikkRequest = NyMottakerIdHistorikkRequest(
      nyStonadOpprettet.stonadId,
      "123",
      "321",
      "Test")
    val nyMottakerIdHistorikkOpprettet = mottakerIdHistorikkService.opprettNyMottakerIdHistorikk(nyMottakerIdHistorikkRequest)

    assertAll(
      Executable { assertThat(nyMottakerIdHistorikkOpprettet).isNotNull() },
      Executable { assertThat(nyMottakerIdHistorikkOpprettet.stonadId).isEqualTo(nyMottakerIdHistorikkRequest.stonadId) },
      Executable { assertThat(nyMottakerIdHistorikkOpprettet.saksbehandlerId).isEqualTo(nyMottakerIdHistorikkRequest.saksbehandlerId) },
      Executable { assertThat(nyMottakerIdHistorikkOpprettet.mottakerIdEndretFra).isEqualTo(nyMottakerIdHistorikkRequest.mottakerIdEndretFra) },
      Executable { assertThat(nyMottakerIdHistorikkOpprettet.mottakerIdEndretTil).isEqualTo(nyMottakerIdHistorikkRequest.mottakerIdEndretTil) }
    )
  }*/

  @Test
  fun `skal finne data for en mottakerIdHistorikk`() {
    // Oppretter nytt mottakerIdHistorikk
    val nyStonadOpprettet = persistenceService.opprettNyStonad(StonadDto(
      stonadType = "BIDRAG", sakId = "SAK-001",
      skyldnerId = "01018011111", kravhaverId = "01010511111", mottakerId = "01018211111",
      opprettetAvSaksbehandlerId = "X123456", endretAvSaksbehandlerId = "X654321"
    ))

    // Oppretter ny mottakerIdHistorikk
    val nyMottakerIdHistorikk = persistenceService.opprettNyMottakerIdHistorikk(
      EndreMottakerIdRequest(
        nyStonadOpprettet.stonadId,
        nyMottakerId = "123",
        saksbehandlerId = "Test"
      )
    )

    // Finner mottakerIdHistorikken som akkurat ble opprettet
    val mottakerIdHistorikkFunnet = mottakerIdHistorikkService.finnAlleEndringerMottakerIdForStonad(nyStonadOpprettet.stonadId)

    assertAll(
      Executable { assertThat(mottakerIdHistorikkFunnet).isNotNull() },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].stonadId).isEqualTo(nyMottakerIdHistorikk.stonadId) },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretFra).isEqualTo(nyMottakerIdHistorikk.mottakerIdEndretFra) },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretTil).isEqualTo(nyMottakerIdHistorikk.mottakerIdEndretTil) },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].saksbehandlerId).isEqualTo(nyMottakerIdHistorikk.saksbehandlerId) },
    )
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }


}
