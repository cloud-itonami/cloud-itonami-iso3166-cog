(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of Republic of the Congo procurement law, whether a claimed
  engagement fee actually equals base + months x rate, whether an
  engagement's own declared prior-ARMP-exclusion duration actually stays
  within Décret n° 2009-156 Art. 146's own five-year statutory cap,
  whether a NIU (Numéro d'Identifiant Unique) registration has been
  verified for a filing that requires it, or when a draft stops being a
  draft and becomes a real-world BOAMP tender response / ARMP filing, so
  this MUST be a separate system able to *reject* a proposal and fall
  back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints).

  This blueprint's own text (docs/business-model.md Trust Controls:
  'any actual BOAMP tender response or ARMP filing submission requires
  Market-Entry Compliance Governor clearance and always escalates to
  human sign-off'; 'a false or fabricated regulatory-requirement claim
  is a HARD hold') names exactly the checks below.

  Seven checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist on file?
    3. Exclusion-duration cap
       exceeded                     -- for `:filing/submit`, when the
                                       engagement declares
                                       `:prior-armp-exclusion? true`,
                                       INDEPENDENTLY recompute whether its
                                       own declared
                                       `:exclusion-duration-years` stays
                                       within Décret n° 2009-156 Art.
                                       146's own five-year statutory
                                       maximum (unless
                                       `:court-ordered-definitive-
                                       exclusion?` is true, Art. 146's
                                       own named exception), and
                                       HARD-hold if so. FLAGSHIP check
                                       for this jurisdiction -- a
                                       STATUTORY CEILING VALIDATION (no
                                       turnover formula, no flat monetary
                                       threshold, no supplier-registry
                                       read, no 3-tier value class, no
                                       bid-evaluation price adjustment,
                                       no sector set-membership), a check
                                       SHAPE genuinely different from
                                       every sibling this catalog's
                                       author has examined. See
                                       `marketentry.facts` /
                                       `marketentry.registry`.
    4. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    5. NIU registration
       unverified                   -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-niu? true`,
                                       INDEPENDENTLY check
                                       `:niu-verified?`. CONDITIONAL
                                       on the engagement's own ground
                                       truth. Grounded in the Direction
                                       Générale des Impôts et des
                                       Domaines (DGID)'s Numéro
                                       d'Identifiant Unique (NIU), via
                                       the Portail Fiscal Officiel
                                       (see `marketentry.facts`).
    6. Hydrocarbons local-content
       non-compliance                -- for `:filing/submit`, when the
                                       engagement declares `:sector
                                       :hydrocarbons`, INDEPENDENTLY
                                       verify BOTH
                                       `:snpc-joint-venture?` and
                                       `:congolese-staffing-compliant?`
                                       are true. SECTOR-CONDITIONAL --
                                       grounded in the 15 novembre 2019
                                       executive order, which applies
                                       ONLY to the petroleum/hydrocarbons
                                       sector (foreign firms must
                                       joint-venture with the Société
                                       Nationale des Pétroles du Congo
                                       (SNPC) and staff 80% of
                                       management / 90% of all positions
                                       with Congolese nationals) -- this
                                       is NOT a general public-
                                       procurement local-content rule,
                                       and this check MUST NEVER fire
                                       for a non-hydrocarbons engagement
                                       (see `marketentry.facts`).
    7. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off dedicated `:drafted?`/`:submitted?` facts (never a `:status`
  value)."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real BOAMP tender-response/ARMP-filing package and
  submitting it are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(RCCM登録/NIU登録/ARMP除外リスト確認/BOAMP登録・代理人確認等)が充足していない状態での提案"}]))))

(defn- exclusion-duration-cap-violations
  "For `:filing/submit`, when the engagement declares
  `:prior-armp-exclusion? true`, INDEPENDENTLY recompute whether its own
  declared exclusion duration stays within Décret n° 2009-156 Art. 146's
  own five-year statutory maximum -- the flagship check this vertical
  adds. Entity-condition-gated (a no-op unless a prior ARMP exclusion is
  actually declared, and never fires when a court-ordered definitive
  exclusion is declared, Art. 146's own named exception)."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (registry/exclusion-duration-exceeds-cap? e)
        [{:rule :exclusion-duration-cap-exceeded
          :detail (str subject " の申告除外期間(" (:exclusion-duration-years e)
                      "年)がDécret n° 2009-156 Art.146の法定上限(" registry/exclusion-duration-cap-years
                      "年)を超過しており、裁判所による確定的除外(court-ordered-definitive-exclusion?)の"
                      "宣言もない -- 提出提案は進められない")}]))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- niu-registration-unverified-violations
  "For `:filing/submit`, when the engagement declares `:requires-niu?
  true`, INDEPENDENTLY check `:niu-verified?` -- CONDITIONAL on the
  engagement's own ground truth. Grounded in the Direction Générale des
  Impôts et des Domaines (DGID)'s Numéro d'Identifiant Unique (NIU)."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:requires-niu? e))
                 (not (true? (:niu-verified? e))))
        [{:rule :niu-registration-unverified
          :detail (str subject " はDirection Générale des Impôts et des Domaines(DGID)へのNIU登録確認を"
                      "要するが未確認 -- 提出提案は進められない")}]))))

(defn- hydrocarbons-local-content-violations
  "For `:filing/submit`, when the engagement declares `:sector
  :hydrocarbons`, INDEPENDENTLY verify BOTH `:snpc-joint-venture?` and
  `:congolese-staffing-compliant?` are true -- grounded in the 15
  novembre 2019 executive order, which applies ONLY to the petroleum/
  hydrocarbons sector (joint-venture with the Société Nationale des
  Pétroles du Congo (SNPC); 80% of management and 90% of all positions
  staffed with Congolese nationals). SECTOR-CONDITIONAL: a
  non-hydrocarbons engagement's `:sector` is never `:hydrocarbons`, so
  this check is a structural no-op for it -- this is deliberately NOT a
  general public-procurement local-content/JV rule."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (= :hydrocarbons (:sector e))
        (when-not (and (true? (:snpc-joint-venture? e))
                       (true? (:congolese-staffing-compliant? e)))
          [{:rule :hydrocarbons-local-content-noncompliant
            :detail (str subject " は炭化水素セクター案件であり、2019年11月15日の大統領令が定める"
                        "SNPC合弁(joint-venture)要件およびコンゴ人スタッフ比率(管理職80%/全体90%)要件を"
                        "独立確認できていない -- 提出提案は進められない")}])))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (exclusion-duration-cap-violations request st)
                           (engagement-fee-mismatch-violations request st)
                           (niu-registration-unverified-violations request st)
                           (hydrocarbons-local-content-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
