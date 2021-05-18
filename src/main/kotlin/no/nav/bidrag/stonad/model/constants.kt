package no.nav.bidrag.stonad.model

// Detaljer (keys) i en VedtakHendelse
object Detalj {
    const val BARNEBIDRAG = "Barnebidrag"
    const val FORSKUDD = "Forskudd"
    const val SAERTILSKUDD = "Saertilskudd"
}

// Verdier til Detaljer i en VedtakHendelse
object DetaljVerdi {
    const val FAGOMRADE_BIDRAG = "BID"
    const val FAGOMRADE_FARSKAP = "FAR"
}

// sproringsdata fra hendelse json
const val CORRELATION_ID = "correlationId"
