(ns statute.facts
  "General-law compliance catalog for the Republic of the Congo (COG) --
  extends this repo's existing `marketentry.facts` (public-procurement
  market-entry only, narrow scope) with a second, orthogonal catalog of
  statutes a company operating in this jurisdiction must generally track
  for compliance. Mirrors cloud-itonami-iso3166-ben/-btn's `statute.facts`
  (ADR-2607141700, cloud-itonami-compliance-fact-federation).

  Every entry below cites an OFFICIAL government-hosted (or, for the
  OHADA entry, official supranational-body-hosted) URL -- never
  fabricated:

  - **Companies/commercial-entity law**: this iteration specifically
    investigated, rather than assumed by analogy to the Benin sibling,
    whether the Republic of the Congo is itself an OHADA member state --
    independently confirmed directly on OHADA's own 'Les Etats membres de
    l'OHADA' page (`ohada.org/les-etats-membres-de-lohada/`,
    WebFetch-verified), which lists 'Congo' among the member states. So,
    like Benin, company law is governed DIRECTLY by a SUPRANATIONAL
    instrument, the OHADA Acte uniforme relatif au droit des sociétés
    commerciales et du groupement d'intérêt économique (AUSCGIE) -- this
    iteration independently re-fetched OHADA's own page
    (`ohada.org/en/commercial-companies-and-economic-interest-groups/`
    and its French counterpart `ohada.org/droit-des-societes-commerciales-
    et-du-gie/`, both WebFetch/curl-verified directly, NOT copied from
    Benin's sibling entry) and confirmed, in OHADA's own words: 'Acte
    uniforme relatif au droit des sociétés commerciales et du groupement
    d'intérêt économique -- Date et lieu d'adoption : 30 janvier 2014 à
    Ouagadougou (Burkina Faso) -- Date de publication au Journal Officiel
    de l'OHADA : 04 février 2014 -- Date d'entrée en vigueur : 05 mai
    2014'. By the OHADA Treaty's own direct-applicability principle
    (Traité de Port-Louis Art. 10, the same finding Benin's sibling
    catalog documents), AUSCGIE applies in the Republic of the Congo
    without any domestic transposition act. Separately, RCCM/business-
    entity REGISTRATION -- as opposed to company FORMATION/governance
    law -- is governed by a DIFFERENT OHADA instrument, the Acte Uniforme
    relatif au Droit Commercial Général (AUDCG); this catalog does not
    conflate the two (`marketentry.facts` cites AUDCG separately for
    RCCM).
  - **Code du Travail (Labour Code)**: 'Code du travail 1975 (modifié
    1996)' per the Secrétariat Général du Gouvernement's (SGG) own
    'Codes en vigueur' listing (`sgg.cg/droit-congolais/les-codes-
    consolides.html`, WebFetch-verified) -- downloaded directly as a PDF
    from `sgg.cg/codes/congo-code-1975-travail.pdf` (the SGG's own
    official hosting). The PDF is a SCANNED document with no native text
    layer (`pdftotext` alone returned zero characters); this iteration
    rendered it to page images (`pdftoppm`) and read it via `tesseract`
    OCR (French-language model, installed for this purpose) directly,
    not a secondary summary. The cover/title page reads: 'LOI N° 45/75 DU
    15 MARS 1975 INSTITUANT UN CODE DU TRAVAIL DE LA REPUBLIQUE POPULAIRE
    DU CONGO', and Article premier (own text): 'La présente loi institue
    un Code du Travail de la République Populaire du Congo.' HIGH
    confidence on the law number (45/75) and year (1975), consistent
    across multiple independent OCR passes; MODERATE confidence on the
    exact day (15 mars) given scanned-image OCR quality rather than a
    native text layer -- an honest, explicitly-flagged confidence
    distinction, not glossed over. This iteration did NOT independently
    fetch/verify the specific 1996 amending law's own number/date SGG's
    own listing references ('modifié 1996') -- an honest, explicit gap.
  - This iteration also independently confirmed, via ARMP's own 'Cadre
    juridique' page (`armp.cg/markets-legal.php`, WebFetch-verified), two
    further Congo-specific statutes relevant to procurement-adjacent
    compliance (not modeled as full catalog entries here, an honest
    scope-narrowing rather than fabricated citations): Loi n° 9-2022 du
    11 mars 2022 (prevention and fight against corruption) and Loi n°
    31-2012 du 11 octobre 2012 (infractions and penalties applicable to
    public-procurement passation and execution) -- both named directly on
    ARMP's own site, neither independently fetched as primary text by
    this iteration, so neither is added as a full `catalog` entry yet.

  A law not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of statute entries. `:statute/url` + `:statute/law-number`
  are the citation the governor requires before any compliance-fact
  proposal referencing this law can commit."
  {"COG"
   [{:statute/id "cog.ohada-auscgie"
     :statute/title "Acte uniforme relatif au droit des sociétés commerciales et du groupement d'intérêt économique (AUSCGIE)"
     :statute/jurisdiction "COG"
     :statute/kind :law
     :statute/law-number "OHADA Uniform Act -- adopted 30 January 2014 (Ouagadougou), published in the OHADA Official Journal 4 February 2014, in force 5 May 2014; directly applicable in the Republic of the Congo as an OHADA member state per Traité de Port-Louis Art. 10, no domestic transposition act required"
     :statute/url "https://www.ohada.org/en/commercial-companies-and-economic-interest-groups/"
     :statute/url-provenance :official-ohada-org
     :statute/enacted-date "2014-01-30"
     :statute/retrieved-at "2026-07-22"
     :statute/topic #{:corporate-governance :incorporation}}
    {:statute/id "cog.code-du-travail-1975"
     :statute/title "Code du Travail de la République Populaire du Congo"
     :statute/jurisdiction "COG"
     :statute/kind :law
     :statute/law-number "Loi n° 45/75 du 15 mars 1975 (own primary text, OCR-read from a scanned PDF -- HIGH confidence on law number/year, MODERATE confidence on the exact day; SGG's own listing describes it as 'modifié 1996', an amendment this iteration did not independently verify)"
     :statute/url "https://sgg.cg/codes/congo-code-1975-travail.pdf"
     :statute/url-provenance :official-sgg-cg
     :statute/enacted-date "1975-03-15"
     :statute/retrieved-at "2026-07-22"
     :statute/topic #{:labor :employment}}]})

(defn spec-basis
  "The jurisdiction's statute vector, or nil -- nil means NO spec-basis
  for that jurisdiction yet."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report, same shape/discipline as `marketentry.facts/coverage`:
  never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-cog statute.facts Wave 0 (ADR-2607141700): "
                 (count (get catalog "COG")) " COG statutes seeded with an "
                 "official citation. Extend "
                 "`statute.facts/catalog`, never fabricate a law-id or URL.")})))

(defn by-topic
  "Statutes for `iso3` tagged with `topic` (e.g. :labor, :corporate-governance)."
  [iso3 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3)))
