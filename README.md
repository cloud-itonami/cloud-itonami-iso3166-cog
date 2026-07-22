# cloud-itonami-iso3166-cog

**COG**: Republic of the Congo.

- ARMP (Autorité de Régulation des Marchés Publics) public procurement, DGCMP a priori control; BOAMP bulletin today, e-procurement digitalization decreed (2024) but not yet live
- OHADA RCCM company/business registration via ACPCE (guichet unique); NIU tax registration via DGID
- Décret n° 2009-156 Art. 146 ARMP exclusion-duration 5-year statutory cap gate

AGPL-3.0-or-later.

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
