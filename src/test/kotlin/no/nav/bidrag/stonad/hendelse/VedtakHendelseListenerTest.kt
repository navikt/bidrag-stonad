package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.stonad.BidragStonadLocal
import no.nav.bidrag.stonad.BidragStonadLocal.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.service.StonadService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragStonadLocal::class])
@DisplayName("VedtakHendelseListener (test av forretningslogikk)")
@ActiveProfiles(TEST_PROFILE)
class VedtakHendelseListenerTest {

    @Autowired
    private lateinit var vedtakHendelseListener: VedtakHendelseListener

    @MockBean
    private lateinit var stonadServiceMock: StonadService

    @Test
    fun `skal lese vedtakshendelse uten feil`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "vedtakType":"MANUELT",
              "stonadType":"BIDRAG",
              "sakId":"",
              "skyldnerId":"",
              "kravhaverId":"",
              "mottakerId":"",
              "opprettetAv":"",
              "opprettetTimestamp":"2022-01-11T10:00:00.000001",
              "periodeListe":[]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `skal behandle barnebidrag`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "vedtakType":"MANUELT",
              "stonadType":"BIDRAG",
              "sakId":"",
              "skyldnerId":"",
              "kravhaverId":"",
              "mottakerId":"",
              "opprettetAv":"",
              "opprettetTimestamp":"2022-01-11T10:00:00.000001",
              "periodeListe":[]
            }
            """.trimIndent()
        )

        verify(stonadServiceMock).hentStonad("BIDRAG", "", "")
    }

    @Test
    fun `skal behandle forskudd`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "vedtakType":"MANUELT",
              "stonadType":"FORSKUDD",
              "sakId":"",
              "skyldnerId":"",
              "kravhaverId":"",
              "mottakerId":"",
              "opprettetAv":"",
              "opprettetTimestamp":"2022-01-11T10:00:00.000001",
              "periodeListe":[]
            }
            """.trimIndent()
        )

        verify(stonadServiceMock).hentStonad("FORSKUDD", "", "")
    }
}