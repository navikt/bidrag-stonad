package no.nav.bidrag.stonad.service

import no.nav.bidrag.behandling.felles.dto.stonad.EndreMottakerIdRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.stonad.OpprettStonadRequestDto
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.persistence.repository.MottakerIdHistorikkRepository
import no.nav.bidrag.stonad.persistence.repository.PeriodeRepository
import no.nav.bidrag.stonad.persistence.repository.StonadRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("MottakerIdHistorikkServiceTest")
@ActiveProfiles(BidragStonadTest.TEST_PROFILE)
@SpringBootTest(classes = [BidragStonadTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
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

  @Test
  fun `skal finne data for en mottakerIdHistorikk`() {
    // Oppretter nytt mottakerIdHistorikk
    val periodeListe = listOf(
      OpprettStonadPeriodeRequestDto(
        periodeFom = LocalDate.parse("2019-01-01"),
        periodeTil = LocalDate.parse("2019-07-01"),
        vedtakId = 321,
        periodeGjortUgyldigAvVedtakId = 246,
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "KOSTNADSBEREGNET_BIDRAG"),
    )
    val nyStonadOpprettetStonadId = persistenceService.opprettNyStonad(
      OpprettStonadRequestDto(
      stonadType = StonadType.BIDRAG, sakId = "SAK-001",
      skyldnerId = "01018011111", kravhaverId = "01010511111", mottakerId = "01018211111",
      opprettetAv = "X123456", indeksreguleringAar = "2024", opphortFra = null,  periodeListe = periodeListe)
    )

    val endreMottakerIdRequest = EndreMottakerIdRequestDto(
      nyStonadOpprettetStonadId,
      nyMottakerId = "123",
      opprettetAv = "Test"
    )

    // Oppretter ny mottakerIdHistorikk
    val nyMottakerIdHistorikkStonadId = persistenceService.opprettNyMottakerIdHistorikk(endreMottakerIdRequest)

    // Finner mottakerIdHistorikken som akkurat ble opprettet
    val mottakerIdHistorikkFunnet = mottakerIdHistorikkService.hentAlleEndringerAvMottakerIdForStonad(nyMottakerIdHistorikkStonadId)

    assertAll(
      Executable { assertThat(mottakerIdHistorikkFunnet).isNotNull() },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].stonadId)
        .isEqualTo(endreMottakerIdRequest.stonadId) },

      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].mottakerIdEndretTil)
        .isEqualTo(endreMottakerIdRequest.nyMottakerId) },
      Executable { assertThat(mottakerIdHistorikkFunnet.alleMottakerIdHistorikkForStonad!![0].opprettetAv)
        .isEqualTo(endreMottakerIdRequest.opprettetAv) },
    )
    mottakerIdHistorikkRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadRepository.deleteAll()
  }


}
