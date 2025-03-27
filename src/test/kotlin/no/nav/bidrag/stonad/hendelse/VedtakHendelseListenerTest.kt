package no.nav.bidrag.stonad.hendelse

import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.stonad.BidragStønadTest
import no.nav.bidrag.stonad.BidragStønadTest.Companion.TEST_PROFILE
import no.nav.bidrag.stonad.service.StønadService
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadRequest
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragStønadTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("VedtakHendelseListener (test av forretningslogikk)")
@ActiveProfiles(TEST_PROFILE)
@EnableMockOAuth2Server
@EnableAspectJAutoProxy
class VedtakHendelseListenerTest {
    @Autowired
    private lateinit var vedtakHendelseListener: VedtakHendelseListener

    @MockBean
    private lateinit var stønadServiceMock: StønadService

    @Test
    fun `skal lese vedtakshendelse uten feil`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "kilde":"MANUELT",
              "type":"ALDERSJUSTERING",
              "id":"1",
              "opprettetAv":"X123456",
              "kildeapplikasjon":"Bisys",              
              "vedtakstidspunkt":"2022-01-11T10:00:00.000001",              
              "enhetsnummer":"Enhet1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stønadsendringListe": [
                {
                 "type": "BIDRAG",
                 "sak": "",
                 "skyldner": "",
                 "kravhaver": "",
                 "mottaker": "",
                 "innkreving": "MED_INNKREVING",
                 "beslutning": "ENDRING",
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":""            
                }
            }
            """.trimIndent(),
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
              "opprettetAv":"X123456",
              "kildeapplikasjon":"Bisys",
              "vedtakstidspunkt":"2022-01-11T10:00:00.000001",                     
              "enhetsnummer":"Enhet1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stønadsendringListe": [
                {
                 "type": "BIDRAG",
                 "sak": "",
                 "skyldner": "",
                 "kravhaver": "",
                 "mottaker": "",
                 "innkreving": "MED_INNKREVING",
                 "beslutning": "ENDRING",
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":""            
                }           
            }
            """.trimIndent(),
        )

        verify(stønadServiceMock).hentStønad(HentStønadRequest(Stønadstype.BIDRAG, Saksnummer(""), Personident(""), Personident("")))
    }

    @Test
    fun `skal behandle forskudd`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "kilde":"MANUELT",
              "type":"ALDERSJUSTERING",
              "id":"1",
              "opprettetAv":"X123456",
              "kildeapplikasjon":"Bisys",
              "vedtakstidspunkt":"2022-01-11T10:00:00.000001",                        
              "enhetsnummer":"Enhet1",
              "opprettetTidspunkt":"2022-01-11T10:00:00.000001",    
              "stønadsendringListe": [
                {
                 "type": "FORSKUDD",
                 "sak": "",
                 "skyldner": "",
                 "kravhaver": "",
                 "mottaker": "",
                 "innkreving": "MED_INNKREVING",
                 "beslutning": "ENDRING",                
                 "periodeListe": []             
              }        
              ],
              "sporingsdata":
                {
                "correlationId":"korrelasjon_id-123213123213"            
                }
            }
            """.trimIndent(),
        )

        verify(stønadServiceMock).hentStønad(
            HentStønadRequest(
                Stønadstype.FORSKUDD,
                Saksnummer(""),
                Personident(""),
                Personident(""),
            ),
        )
    }

    @Test
    fun `skal behandle vedtak med kun særbidrag, skal ikke kaste exception`() {
        vedtakHendelseListener.lesHendelse(
            """
            {
              "kilde":"MANUELT",
              "type":"ENDRING",
              "id":4756855,
              "opprettetAv":"A12345",
              "opprettetAvNavn":"Testesen, Test",
              "kildeapplikasjon":"bisys",
              "vedtakstidspunkt":"2024-08-01T12:58:09.000467",
              "enhetsnummer":"4833",
              "innkrevingUtsattTilDato":null,
              "fastsattILand":null,
              "opprettetTidspunkt":"2024-08-01T14:58:09.878217031",
              "stønadsendringListe":[],
              "engangsbeløpListe":[
              {
               "type":"SÆRBIDRAG",
               "sak":"1234567",
               "skyldner":"12345678901",
               "kravhaver":"10987654321",
               "mottaker":"111222333660",
               "beløp":399.00,
               "valutakode":"NOK",
               "resultatkode":"VS",
               "innkreving":"MED_INNKREVING",
               "beslutning":"ENDRING",
               "omgjørVedtakId":null,
               "referanse":"Referanse",
               "delytelseId":"1234567",
               "eksternReferanse":null,
               "betaltBeløp":null}
               ],
               "behandlingsreferanseListe":[
               {
                "kilde":"BISYS_SØKNAD",
                "referanse":"1234567"
                }
                ],
                "sporingsdata":
                {
                 "correlationId":"0000",
                 "brukerident":null,
                 "opprettet":"2024-08-01T14:58:09.889834617",
                 "opprettetAv":null
                 }
                 }
            """.trimIndent(),
        )
    }
}
