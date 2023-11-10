FROM ghcr.io/navikt/baseimages/temurin:21
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./target/bidrag-stønad-*.jar app.jar
COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle

EXPOSE 8080

ENV ENVOY_ADMIN_API=http://127.0.0.1:15000
ENV SPRING_PROFILES_ACTIVE=nais