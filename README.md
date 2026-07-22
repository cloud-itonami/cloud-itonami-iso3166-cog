# cloud-itonami-iso3166-cog

**`:implemented`** for **COG** (Republic of the Congo / Congo-Brazzaville --
NOT the Democratic Republic of the Congo, `cloud-itonami-iso3166-cod`, a
separate repo). Independent public-sector market-entry & procurement
compliance actor: a MarketEntry-LLM advisor sealed behind a Market-Entry
Compliance Governor, a langgraph-clj StateGraph, and an append-only audit
ledger (see `orgs/cloud-itonami/cloud-itonami-iso3166-ago`'s
`marketentry.*` for this fleet's canonical structural template).

```
clojure -M:dev:test
```

## Governor checks (all HARD, human-unoverridable) and their sources

1. **Spec-basis** -- a `:jurisdiction/assess`/`:filing/draft`/
   `:filing/submit` proposal must cite an official source
   (`marketentry.facts`), never an invented one.
2. **Evidence-checklist completeness** -- RCCM (OHADA commercial
   register) + NIU (tax ID) + ARMP exclusion-list confirmation +
   BOAMP/ARMP tender-participation registration must actually be on
   file before a filing draft/submit.
   Sources: <https://acpce.cg/> , <https://rccm.ohada.org/staticPage/index?alias=cg> ,
   <https://www.niu.cg/> , <https://www.armp.cg/disputes-excluded.php?lang=fr>
3. **Exclusion-duration statutory cap (flagship check)** -- an
   engagement's own declared prior-ARMP-exclusion duration must stay
   within Décret n° 2009-156 du 20 mai 2009 (Code des Marchés
   Publics), Art. 146's own five-year maximum, unless a competent
   court has pronounced a definitive (permanent) exclusion for repeat
   violations -- Art. 146's own named exception.
   Source: <https://sgg.cg/codes/congo-code-2009-marches-publics.pdf>
4. **Engagement-fee ground-truth recompute** -- this actor's own
   service fee (`base-fee + monthly-rate x monitoring-months`) is
   independently recomputed against the claimed total before submit.
5. **NIU registration verification** -- the Numéro d'Identifiant
   Unique, issued by the Direction Générale des Impôts et des
   Domaines (DGID), is legally mandatory for the signature of any
   financial-commitment contract or public contract.
   Sources: <https://www.finances.gouv.cg/fr/articles/num%C3%A9ro-didentification-unique-niu> ,
   <https://www.niu.cg/> , <https://impots.gouv.cg/portail-client-web/>
6. **Hydrocarbons-sector-CONDITIONAL local content / SNPC joint
   venture** -- ONLY when an engagement's own `:sector` is
   `:hydrocarbons`: independently verify an SNPC joint-venture and
   Congolese-staffing compliance (80% management / 90% overall),
   per the 15 November 2019 executive order. This is deliberately
   NOT a general public-procurement local-content rule and this check
   MUST NEVER fire for a non-hydrocarbons engagement (see
   `test/marketentry/governor_contract_test.clj`).
   Source: <https://www.state.gov/reports/2022-investment-climate-statements/republic-of-the-congo>
7. **Confidence floor / actuation gate** -- low advisor confidence, or
   the op being a real actuation (`:filing/draft`/`:filing/submit`),
   always escalates to a human -- see Actuation below.

Two more structural guards (not part of the numbered list above,
present in every actor this fleet has shipped): double-draft and
double-submit prevention off dedicated `:drafted?`/`:submitted?`
facts.

A procurement threshold-tiered procedure regime also exists (Décret
n° 2009-162 du 20 mai 2009, amended by Décret n° 2011-843) but the
actual numeric thresholds could not be independently verified this
iteration -- deliberately NOT modeled as a governor check with
fabricated numbers.

## Actuation -- `:filing/draft` and `:filing/submit` are ALWAYS human-gated

Drafting a real BOAMP tender response / ARMP filing package
(`:filing/draft`) and actually submitting one (`:filing/submit`) are
the two real-world acts this actor performs. Two independent layers
agree neither ever auto-commits, at ANY rollout phase (0 through 3):

- `marketentry.governor`'s `high-stakes` set (`:actuation/draft-filing`
  `:actuation/submit-filing`) always forces an `:escalate` disposition,
  even when every hard check is clean.
- `marketentry.phase`'s per-phase `:auto` sets deliberately never
  include `:filing/draft`/`:filing/submit`, including at phase 3
  (`supervised-auto`) -- see the namespace docstring's explicit note
  not to add them there.

`interrupt-before #{:request-approval}` (`marketentry.operation`)
pauses the StateGraph run at this point; only an explicit human
`{:status :approved}` resume lets the record commit to the SSoT.

## Culture catalog

This repo carries a **country-level regional-culture catalog**
(ADR-2607171400 addendum 2, `cloud-itonami-municipality-culture-catalog`
Wave 1, in `com-junkawasaki/root`) — national dishes, protected products,
beverages, crafts, festivals and heritage sites for the Republic of the
Congo:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
