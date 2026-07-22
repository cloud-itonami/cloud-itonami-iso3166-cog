(ns statute.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest cog-has-spec-basis
  (let [sb (facts/spec-basis "COG")]
    (is (= 2 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://") sb))
    (is (every? :statute/law-number sb))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["COG" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["cog.code-du-travail-1975"]
         (mapv :statute/id (facts/by-topic "COG" :labor))))
  (is (= ["cog.ohada-auscgie"]
         (mapv :statute/id (facts/by-topic "COG" :corporate-governance))))
  (is (empty? (facts/by-topic "COG" :environment)))
  (is (empty? (facts/by-topic "ATL" :labor))))
