(ns marketentry.registry
  "Pure-function market-entry filing-draft + filing-submit record
  construction -- an append-only market-entry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for a public-procurement market-entry
  filing -- every jurisdiction assigns its own format. This namespace
  does NOT invent one; it builds a jurisdiction-scoped sequence number
  and validates the record's required fields, the same honest,
  non-fabricating discipline `marketentry.facts` uses.

  `engagement-fee-matches-claim?` is an HONEST reapplication of the
  SAME ground-truth-recompute DISCIPLINE sibling actors use (verify a
  claimed monetary total against the entity's own recorded quantity x
  unit fields), reapplied to a market-entry engagement fee line.

  `exclusion-duration-exceeds-cap?` is THIS vertical's own new ground-
  truth check, grounding COG's flagship governor check
  (`marketentry.governor/exclusion-duration-cap-violations`): Décret n°
  2009-156 du 20 mai 2009 (Code des Marchés Publics), Art. 146 (own
  primary text, see `marketentry.facts`), caps ANY ARMP exclusion-from-
  competition sanction at five years -- 'La décision d'exclusion de la
  commande publique ne peut dépasser cinq ans' -- with exactly ONE named
  exception: a competent court's OWN definitive (permanent) exclusion
  order following repeat violations by the same person. This is a
  DIFFERENT check SHAPE from every sibling this catalog's author has
  examined: not a turnover-scaled formula (Bulgaria), not a flat
  statutory threshold on a monetary value (Albania), not a boolean
  registry-membership read of the SUPPLIER (Azerbaijan/Armenia/Bolivia),
  not a 3-tier contract-value classification (Antigua and Barbuda), not a
  bid-evaluation price-adjustment recompute (Benin), not a set-membership
  check on a declared sector (Bhutan) -- it is a STATUTORY CEILING
  VALIDATION on an engagement's OWN self-declared prior-sanction DURATION
  (a number of years), with a single named conditional exception branch.
  The cap itself (`exclusion-duration-cap-years`) is NEVER hardcoded
  independently of `marketentry.facts` semantics -- it mirrors the same
  single-source-of-truth discipline `fdi-sector-restricted?`-style
  siblings use for their own negative-list/threshold values, though here
  the constant is stable enough (a fixed statutory number, not a mutable
  published list) that this namespace defines it directly and
  `marketentry.facts/exclusion-cap-spec-basis` cites the identical value
  for the governor's citation trail.

  It is entity-condition-gated like Bhutan's FDI check: a no-op (false)
  unless `:prior-armp-exclusion?` is true (an engagement with no prior
  ARMP sanction at all has nothing to validate), and the numeric cap
  itself never applies when `:court-ordered-definitive-exclusion?` is
  true -- Art. 146's own second sentence permits an UNCAPPED (permanent)
  exclusion once a competent court, not ARMP alone, has pronounced it for
  a repeat offender. Missing `:exclusion-duration-years` for a declared
  prior exclusion is never treated as exceeding the cap HERE (that is the
  `evidence-incomplete` check's job, upstream, where an assessment must
  already exist).

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real ARMP, DGCMP, ACPCE or DGID system. It builds the
  RECORD an operator would keep, not the act of submitting a BOAMP
  tender response or an ARMP filing itself (that is
  `marketentry.operation`'s `:filing/submit`, always human-gated -- see
  README Actuation)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the market-entry operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn compute-engagement-fee
  "The ground-truth engagement fee for `engagement`'s own `:base-fee`
  and `:monitoring-months` x `:monthly-rate` -- a single flat
  base + months x rate calculation, not a full pricing engine."
  [{:keys [base-fee monthly-rate monitoring-months]}]
  (+ (double base-fee)
     (* (double monthly-rate) (double monitoring-months))))

(defn engagement-fee-matches-claim?
  "Does `engagement`'s own `:claimed-fee` equal the independently
  recomputed `compute-engagement-fee`?"
  [{:keys [claimed-fee] :as engagement}]
  (== (double claimed-fee) (compute-engagement-fee engagement)))

(def exclusion-duration-cap-years
  "Décret n° 2009-156 du 20 mai 2009, Art. 146 (own primary text,
  curl/pdftotext-verified 2026-07-22 against sgg.cg's official hosting):
  'La décision d'exclusion de la commande publique ne peut dépasser cinq
  ans.' The one named exception (a court-ordered DEFINITIVE exclusion for
  a repeat offender) has no numeric cap at all -- see
  `exclusion-duration-exceeds-cap?`."
  5)

(defn exclusion-duration-exceeds-cap?
  "Does `engagement`'s own declared prior-ARMP-exclusion duration exceed
  Art. 146's own five-year statutory maximum?

  A no-op (false) unless `:prior-armp-exclusion?` is true -- an
  engagement with no declared prior ARMP sanction has nothing for this
  check to validate. When `:court-ordered-definitive-exclusion?` is true,
  this check NEVER fires regardless of `:exclusion-duration-years` --
  Art. 146's own second sentence permits an uncapped, permanent exclusion
  once pronounced by a competent court for repeat violations, a real,
  named, conditional exception, not a loophole this namespace invented.
  Missing/non-numeric `:exclusion-duration-years` for a declared prior
  exclusion is also never treated as exceeding the cap here (that is the
  `evidence-incomplete` check's job, upstream, where an assessment must
  already exist)."
  [{:keys [prior-armp-exclusion? exclusion-duration-years
           court-ordered-definitive-exclusion?]}]
  (boolean
   (when (true? prior-armp-exclusion?)
     (when-not (true? court-ordered-definitive-exclusion?)
       (when (number? exclusion-duration-years)
         (> exclusion-duration-years exclusion-duration-cap-years))))))

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT -- the
  market-entry operator's own act of preparing a BOAMP/ARMP tender
  response or filing package. Pure function -- does not touch any real
  ARMP, DGCMP, ACPCE or DGID system."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "draft: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str (str/upper-case jurisdiction) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT -- the
  market-entry operator's own act of actually submitting a BOAMP/ARMP
  tender response or filing (always human-gated upstream)."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "submit: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str (str/upper-case jurisdiction) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
