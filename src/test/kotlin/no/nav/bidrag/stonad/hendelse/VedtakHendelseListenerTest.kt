package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.behandling.felles.dto.stonad.HentStonadRequest
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.stonad.BidragStonadTest
import no.nav.bidrag.stonad.BidragStonadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.service.StonadService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragStonadTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("VedtakHendelseListener (test av forretningslogikk)")
@ActiveProfiles(TEST_PROFILE)
@EnableMockOAuth2Server
@EnableAspectJAutoProxy
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
              "kilde":"MANUELT",
              "type":"ALDERSJUSTERING",
              "id":"1",
              "vedtakTidspunkt":"2022-01-11T10:00:00.000001",              
              "enhetId":"Enhet1",
              "opprettetAv":"Saksbehandler1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stonadsendringListe": [
                {
                 "type": "BIDRAG",
                 "sakId": "",
                 "skyldnerId": "",
                 "kravhaverId": "",
                 "mottakerId": "",
                 "innkreving": "JA",
                 "endring": "true",
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":""            
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `skal behandle barnebidrag`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "kilde":"MANUELT",
              "type":"ALDERSJUSTERING",
              "id":"1",
              "vedtakTidspunkt":"2022-01-11T10:00:00.000001",                     
              "enhetId":"Enhet1",
              "opprettetAv":"Saksbehandler1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stonadsendringListe": [
                {
                 "type": "BIDRAG",
                 "sakId": "",
                 "skyldnerId": "",
                 "kravhaverId": "",
                 "mottakerId": "",
                 "innkreving": "JA",
                 "endring": "true",
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":""            
                }           
            }
            """.trimIndent()
        )

        verify(stonadServiceMock).hentStonad(HentStonadRequest(StonadType.BIDRAG, "", "", ""))
    }

    @Test
    fun `skal behandle forskudd`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "kilde":"MANUELT",
              "type":"ALDERSJUSTERING",
              "id":"1",
              "vedtakTidspunkt":"2022-01-11T10:00:00.000001",                        
              "enhetId":"Enhet1",
              "opprettetAv":"Saksbehandler1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stonadsendringListe": [
                {
                 "type": "FORSKUDD",
                 "sakId": "",
                 "skyldnerId": "",
                 "kravhaverId": "",
                 "mottakerId": "",
                 "innkreving": "JA",
                 "endring": "true",                 
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":"korrelasjon_id-123213123213"            
                }
            }
            """.trimIndent()
        )

        verify(stonadServiceMock).hentStonad(HentStonadRequest(StonadType.FORSKUDD, "", "", ""))
    }
}
