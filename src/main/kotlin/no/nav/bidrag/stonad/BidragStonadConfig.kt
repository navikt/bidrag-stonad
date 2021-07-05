package no.nav.bidrag.stonad

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.stonad.hendelse.KafkaVedtakHendelseListener
import no.nav.bidrag.stonad.service.BehandleHendelseService
import no.nav.bidrag.stonad.service.JsonMapperService
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.messaging.Message
import java.util.Optional

const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-stonad", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")])
@EnableJwtTokenValidation
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP
)
class BidragStonadConfig {

    @Bean
    fun exceptionLogger(): ExceptionLogger {
        return ExceptionLogger(BidragStonad::class.java.simpleName)
    }

    @Bean
    fun correlationIdFilter(): CorrelationIdFilter {
        return CorrelationIdFilter()
    }
}

val LOGGER = LoggerFactory.getLogger(KafkaConfig::class.java)

@Bean
fun oidcTokenManager(tokenValidationContextHolder: TokenValidationContextHolder?): OidcTokenManager? {
    return OidcTokenManager {
        Optional.ofNullable(tokenValidationContextHolder)
            .map { obj: TokenValidationContextHolder -> obj.tokenValidationContext }
            .map { tokenValidationContext: TokenValidationContext ->
                tokenValidationContext.getJwtTokenAsOptional(ISSUER)
            }
            .map { obj: Optional<JwtToken?> -> obj.get() }
            .map { obj: JwtToken -> obj.tokenAsString }
            .orElseThrow {
                IllegalStateException(
                    "Kunne ikke videresende Bearer-token"
                )
            }
    }
}

fun interface OidcTokenManager {
    fun hentIdToken(): String?
}

@Configuration
@Profile(LIVE_PROFILE)
class KafkaConfig {
    @Bean
    fun vedtakHendelseListener(
        jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService
    ) = KafkaVedtakHendelseListener(jsonMapperService, behandeHendelseService)

    @Bean
    fun vedtakshendelseErrorHandler(): KafkaListenerErrorHandler {
        return KafkaListenerErrorHandler { message: Message<*>, e: ListenerExecutionFailedException ->
            val messagePayload: Any = try {
                message.payload
            } catch (re: RuntimeException) {
                "Unable to read message payload"
            }

            LOGGER.error("Message {} cause error: {} - {} - headers: {}", messagePayload, e.javaClass.simpleName, e.message, message.headers)
            Optional.empty<Any>()
        }
    }
}