# bidrag-stonad

![](https://github.com/navikt/bidrag-stonad/workflows/continuous%20integration/badge.svg)
[![test build on pull request](https://github.com/navikt/bidrag-stonad/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-stonad/actions/workflows/pr.yaml)
[![release bidrag-stonad](https://github.com/navikt/bidrag-stonad/actions/workflows/release.yaml/badge.svg)](https://github.com/navikt/bidrag-stonad/actions/workflows/release.yaml)


Repo for behandling av stønad i Bidrag.
Ved nye vedtak for en stønad vil alltid periodene i det nye vedtaket erstatte eksisterende perioder i stønaden.
Ved overlapp vil eksisterende perioder merkes som ugyldiggjorte og nye perioder med identiske verdier opprettes 
for periodene som eventuelt ikke dekkes av det nye vedtaket.

