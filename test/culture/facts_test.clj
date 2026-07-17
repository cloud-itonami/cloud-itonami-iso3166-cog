(ns culture.facts-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [culture.facts :as facts]))

(deftest cog-has-culture-basis
  (let [sb (facts/spec-basis "COG")]
    (is (= 8 (count sb)))
    (is (= (count sb) (count (set (map :culture/id sb)))))
    (is (every? #(str/starts-with? (:culture/url %) "https://") sb))
    (is (every? #(= "COG" (:culture/country %)) sb))
    (is (every? #(nil? (:culture/municipality %)) sb))
    (is (every? #(seq (:culture/summary %)) sb))
    (is (every? #(string? (:culture/retrieved-at %)) sb))))

(deftest unknown-jurisdiction-has-no-basis
  (is (nil? (facts/spec-basis "COD")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["COG" "COD"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["COD"] (:missing-jurisdictions c)))))

(deftest by-kind-filters
  (is (= 3 (count (facts/by-kind "COG" :dish))))
  (is (= ["cog.beverage.palm-wine"]
         (mapv :culture/id (facts/by-kind "COG" :beverage))))
  (is (empty? (facts/by-kind "COG" :other)))
  (is (empty? (facts/by-kind "COD" :dish))))

(deftest tx-file-matches-catalog
  (let [tx (edn/read-string (slurp "data/culture-tx.edn"))
        flat (mapcat val (sort-by key facts/catalog))]
    (is (= (vec flat) (vec tx)))))
