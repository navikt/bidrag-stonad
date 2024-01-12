# bidrag-stønad

![](https://github.com/navikt/bidrag-stonad/workflows/continuous%20integration/badge.svg)
[![test build on pull request](https://github.com/navikt/bidrag-stonad/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-stonad/actions/workflows/pr.yaml)
[![release bidrag-stonad](https://github.com/navikt/bidrag-stonad/actions/workflows/release.yaml/badge.svg)](https://github.com/navikt/bidrag-stonad/actions/workflows/release.yaml)


Repo for behandling av stønad i Bidrag.
Ved nye vedtak for en stønad vil alltid periodene i det nye vedtaket erstatte eksisterende perioder i stønaden.
Ved overlapp vil eksisterende perioder merkes som ugyldiggjorte og nye perioder med identiske verdier opprettes 
for periodene som eventuelt ikke dekkes av det nye vedtaket.

#### Kjøre lokalt mot sky
For å kunne kjøre lokalt mot sky må du gjøre følgende

Åpne terminal på root mappen til `bidrag-stønad`
Konfigurer kubectl til å gå mot kluster `dev-gcp`
```bash
# Sett cluster til dev-gcp
kubectx dev-gcp
# Sett namespace til bidrag
kubens bidrag 
# -- Eller hvis du ikke har kubectx/kubens installert 
# (da må -n=bidrag legges til etter exec i neste kommando)
kubectl config use dev-gcp
```
Deretter kjør følgende kommando for å importere secrets. Viktig at filen som opprettes ikke committes til git

```bash
kubectl exec --tty deployment/bidrag-stonad-feature printenv | grep -E 'AZURE_|_URL|SCOPE|TOPIC' > src/test/resources/application-lokal-nais-secrets.properties
