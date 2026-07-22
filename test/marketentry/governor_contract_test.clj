(ns marketentry.governor-contract-test
  "The governor contract as executable tests -- this vertical's own
  Trust Controls implemented faithfully. The single invariant under test:

    MarketEntry-LLM never drafts or submits a filing the Market-Entry
    Compliance Governor would reject, `:filing/draft`/`:filing/submit`
    NEVER auto-commit at any phase, `:engagement/intake` MAY auto-commit
    when clean, and every decision (commit OR hold) leaves exactly one
    ledger fact."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [marketentry.store :as store]
            [marketentry.operation :as op]))

(defn- fresh []
  (let [db (store/seed-db)]
    [db (op/build db)]))

(def operator {:actor-id "op-1" :actor-role :market-entry-operator :phase 3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- assess!
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-assess") {:op :jurisdiction/assess :subject subject} operator)
  (approve! actor (str tid-prefix "-assess")))

(defn- draft!
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-draft") {:op :filing/draft :subject subject} operator)
  (approve! actor (str tid-prefix "-draft")))

(deftest clean-intake-auto-commits
  (let [[db actor] (fresh)
        res (exec-op actor "t1"
                  {:op :engagement/intake :subject "eng-1"
                   :patch {:id "eng-1" :operator "Brazzaville Négoce SARL"}} operator)]
    (is (= :commit (get-in res [:state :disposition])))
    (is (= "Brazzaville Négoce SARL" (:operator (store/engagement db "eng-1"))) "SSoT actually updated")
    (is (= 1 (count (store/ledger db))))))

(deftest jurisdiction-assess-always-needs-approval
  (testing "assess is never in any phase's :auto set -- always human approval, even when clean"
    (let [[db actor] (fresh)
          res (exec-op actor "t2" {:op :jurisdiction/assess :subject "eng-1"} operator)]
      (is (= :interrupted (:status res)))
      (let [r2 (approve! actor "t2")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (some? (store/assessment-of db "eng-1")))))))

(deftest fabricated-jurisdiction-is-held
  (testing "a jurisdiction/assess proposal with no official spec-basis -> HOLD"
    (let [[db actor] (fresh)
          res (exec-op actor "t3"
                    {:op :jurisdiction/assess :subject "eng-1" :no-spec? true} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:no-spec-basis} (-> (store/ledger db) first :basis)))
      (is (nil? (store/assessment-of db "eng-1")) "no assessment written"))))

(deftest draft-without-assessment-is-held
  (testing "filing/draft before any jurisdiction assessment -> HOLD (evidence incomplete)"
    (let [[db actor] (fresh)
          res (exec-op actor "t4" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:evidence-incomplete} (-> (store/ledger db) first :basis))))))

(deftest exclusion-duration-cap-exceeded-is-held-and-unoverridable
  (testing "declared prior-ARMP-exclusion duration (7 years, no court-ordered definitive exclusion) exceeds Décret n° 2009-156 Art.146's 5-year cap -> HARD hold (flagship check)"
    (let [[db actor] (fresh)
          _ (assess! actor "t5pre" "eng-4")
          _ (draft! actor "t5pre" "eng-4")
          res (exec-op actor "t5" {:op :filing/submit :subject "eng-4"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:exclusion-duration-cap-exceeded} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest engagement-fee-mismatch-is-held
  (testing "claimed fee that doesn't equal base + months x rate -> HOLD"
    (let [[db actor] (fresh)
          _ (assess! actor "t6pre" "eng-3")
          _ (draft! actor "t6pre" "eng-3")
          res (exec-op actor "t6" {:op :filing/submit :subject "eng-3"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:engagement-fee-mismatch} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest niu-registration-unverified-is-held-and-unoverridable
  (testing "unverified DGID NIU registration when required -> HARD hold"
    (let [[db actor] (fresh)
          _ (assess! actor "t7pre" "eng-5")
          _ (draft! actor "t7pre" "eng-5")
          res (exec-op actor "t7" {:op :filing/submit :subject "eng-5"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:niu-registration-unverified} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest court-ordered-definitive-exclusion-is-not-gated-by-the-cap-check
  (testing "eng-6 declares a 20-year exclusion, but ALSO :court-ordered-definitive-exclusion? true -- Art.146's own named exception, so the flagship check never fires for it"
    (let [[_db actor] (fresh)
          _ (assess! actor "t14pre" "eng-6")
          _ (draft! actor "t14pre" "eng-6")
          res (exec-op actor "t14" {:op :filing/submit :subject "eng-6"} operator)]
      (is (= :interrupted (:status res)) "clean submit still escalates for human approval, not held")
      (let [r2 (approve! actor "t14")]
        (is (= :commit (get-in r2 [:state :disposition])))))))

(deftest hydrocarbons-local-content-noncompliant-is-held-and-unoverridable
  (testing "hydrocarbons-sector engagement with no SNPC joint-venture and no Congolese-staffing compliance -> HARD hold (15 Nov 2019 executive order)"
    (let [[db actor] (fresh)
          _ (assess! actor "t12pre" "eng-7")
          _ (draft! actor "t12pre" "eng-7")
          res (exec-op actor "t12" {:op :filing/submit :subject "eng-7"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:hydrocarbons-local-content-noncompliant} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest hydrocarbons-local-content-check-never-fires-for-non-hydrocarbons-sector
  (testing "SAME non-compliant SNPC-JV/staffing flags as eng-7, but :sector :general -> the hydrocarbons check is a structural no-op, submit proceeds to the normal human-approval escalation instead of a HARD hold"
    (let [[db actor] (fresh)
          _ (assess! actor "t13pre" "eng-8")
          _ (draft! actor "t13pre" "eng-8")
          res (exec-op actor "t13" {:op :filing/submit :subject "eng-8"} operator)]
      (is (= :interrupted (:status res))
          "clean submit still escalates for ordinary human approval -- not HARD-held by the hydrocarbons-only rule")
      (let [r2 (approve! actor "t13")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (true? (:submitted? (store/engagement db "eng-8"))))))))

(deftest submit-always-escalates-then-human-decides
  (testing "a clean fully-assessed submit still ALWAYS interrupts for human approval"
    (let [[db actor] (fresh)
          _ (assess! actor "t8pre" "eng-1")
          _ (draft! actor "t8pre" "eng-1")
          r1 (exec-op actor "t8" {:op :filing/submit :subject "eng-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, submit record drafted"
        (let [r2 (approve! actor "t8")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:submitted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/submit-history db))) "one draft submit record"))))))

(deftest draft-always-escalates-then-human-decides
  (testing "a clean fully-assessed draft still ALWAYS interrupts for human approval"
    (let [[db actor] (fresh)
          _ (assess! actor "t9pre" "eng-1")
          r1 (exec-op actor "t9" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, draft record drafted"
        (let [r2 (approve! actor "t9")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:drafted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/draft-history db))) "one draft record"))))))

(deftest engagement-double-draft-is-held
  (testing "drafting the same engagement twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (assess! actor "t10pre" "eng-1")
          _ (draft! actor "t10pre" "eng-1")
          res (exec-op actor "t10" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-drafted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/draft-history db))) "still only the one earlier draft"))))

(deftest engagement-double-submit-is-held
  (testing "submitting the same engagement twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (assess! actor "t11pre" "eng-1")
          _ (draft! actor "t11pre" "eng-1")
          _ (exec-op actor "t11a" {:op :filing/submit :subject "eng-1"} operator)
          _ (approve! actor "t11a")
          res (exec-op actor "t11" {:op :filing/submit :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-submitted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/submit-history db))) "still only the one earlier submit"))))

(deftest every-decision-leaves-one-ledger-fact
  (testing "write-only-through-ledger: N operations -> N ledger facts"
    (let [[db actor] (fresh)]
      (exec-op actor "a" {:op :engagement/intake :subject "eng-1"
                          :patch {:id "eng-1" :operator "Brazzaville Négoce SARL"}} operator)
      (exec-op actor "b" {:op :jurisdiction/assess :subject "eng-1" :no-spec? true} operator)
      (is (= 2 (count (store/ledger db)))
          "one commit + one hold, both recorded"))))
