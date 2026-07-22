(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest cog-has-spec-basis
  (let [sb (facts/spec-basis "COG")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/corporate-number-spec-basis "COG")))
    (is (some? (facts/business-registration-spec-basis "COG")))
    (is (some? (facts/exclusion-cap-spec-basis "COG")))))

(deftest cog-rep-spec-basis-is-honestly-absent
  (testing "Décret n° 2009-156 Art. 146's exclusion-extension provision covers majority-capital parent/subsidiary companies, not a bidder's own representatives/directors -- deliberately not claimed"
    (is (nil? (facts/rep-spec-basis "COG")))))

(deftest cog-business-registration-is-a-different-body-from-tax-and-procurement
  (testing "business/company registration (ACPCE) and tax registration (DGID) are administered by different authorities -- see namespace docstring"
    (let [reg (facts/business-registration-spec-basis "COG")
          tax (facts/corporate-number-spec-basis "COG")]
      (is (some? reg))
      (is (some? tax))
      (is (not= (:business-registration-owner-authority reg)
                (:corporate-number-owner-authority tax))))))

(deftest cog-exclusion-cap-is-the-flagship-spec-basis
  (testing "Décret n° 2009-156 Art. 146's five-year exclusion-duration cap is a real, verifiable statutory maximum -- not fabricated"
    (let [ec (facts/exclusion-cap-spec-basis "COG")]
      (is (some? ec))
      (is (= 5 (:exclusion-cap-years ec)))
      (is (string? (:exclusion-cap-legal-basis ec))))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ")))
  (is (nil? (facts/business-registration-spec-basis "ATL")))
  (is (nil? (facts/exclusion-cap-spec-basis "ATL"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "COG")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "COG" all)))
    (is (not (facts/required-evidence-satisfied? "COG" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["COG" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["ATL"] (:missing-jurisdictions c)))))
