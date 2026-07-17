(ns culture.facts
  "Country-level regional-culture catalog for the Republic of the Congo
  (COG) -- national dishes, protected products, beverages, crafts,
  festivals and heritage sites, per ADR-2607171400 addendum 2
  (cloud-itonami-municipality-culture-catalog Wave 1, in
  com-junkawasaki/root). Sibling namespace to `marketentry.facts` /
  `statute.facts` (ADR-2607141700); city-level counterparts live in the
  cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).
  COG is the Republic of the Congo (Brazzaville); the DR Congo
  (Kinshasa) is COD, a separate sibling catalog.

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"COG"
   [{:culture/id "cog.dish.moambe-chicken"
     :culture/name "Moambe chicken"
     :culture/name-local "Muamba nsusu"
     :culture/country "COG"
     :culture/kind :dish
     :culture/summary "Savoury chicken dish made with palm butter, described as a national dish of the Republic of the Congo, where a peanut-butter version is called muamba nsusu; also a national dish of the Democratic Republic of the Congo and Angola."
     :culture/url "https://en.wikipedia.org/wiki/Moambe_chicken"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.dish.chikwangue"
     :culture/name "Chikwangue"
     :culture/name-local "Kwanga"
     :culture/country "COG"
     :culture/kind :dish
     :culture/summary "Starchy fermented-cassava staple wrapped in leaves and steamed or boiled, eaten across Central Africa including the Republic of the Congo, where it is known in Lingala as kwanga."
     :culture/url "https://en.wikipedia.org/wiki/Chikwangue"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.dish.fufu"
     :culture/name "Fufu"
     :culture/name-local "Moteke"
     :culture/country "COG"
     :culture/kind :dish
     :culture/summary "Pounded starchy meal eaten across West and Central Africa; in Congo-Brazzaville it is known by names including fufú, moteke, luku and bidia."
     :culture/url "https://en.wikipedia.org/wiki/Fufu"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.beverage.palm-wine"
     :culture/name "Palm wine"
     :culture/name-local "Nsámbá"
     :culture/country "COG"
     :culture/kind :beverage
     :culture/summary "Alcoholic beverage from fermented palm sap, important in ceremonies across Central and Western Africa; in both Congos it is called nsámbá."
     :culture/url "https://en.wikipedia.org/wiki/Palm_wine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.heritage.congolese-rumba"
     :culture/name "Congolese rumba"
     :culture/country "COG"
     :culture/kind :heritage
     :culture/summary "Dance-music genre that emerged in Brazzaville and Léopoldville (now Kinshasa), added in December 2021 to the UNESCO list of intangible cultural heritage jointly for the Republic of the Congo and the Democratic Republic of the Congo."
     :culture/url "https://en.wikipedia.org/wiki/Congolese_rumba"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.heritage.sangha-trinational"
     :culture/name "Sangha Trinational"
     :culture/country "COG"
     :culture/kind :heritage
     :culture/summary "Transnational Congo Basin rainforest protected area shared by the Republic of the Congo, Cameroon and the Central African Republic, added as a UNESCO World Heritage Site in 2012."
     :culture/url "https://en.wikipedia.org/wiki/Sangha_Trinational"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.heritage.nouabale-ndoki"
     :culture/name "Nouabalé-Ndoki National Park"
     :culture/country "COG"
     :culture/kind :heritage
     :culture/summary "Pristine tropical-rainforest national park in the northern Republic of the Congo, established in 1993; awarded World Heritage status in 2012 as part of the Sangha Trinational."
     :culture/url "https://en.wikipedia.org/wiki/Nouabalé-Ndoki_National_Park"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "cog.heritage.odzala-kokoua"
     :culture/name "Odzala-Kokoua National Park"
     :culture/country "COG"
     :culture/kind :heritage
     :culture/summary "National park in northwestern Republic of the Congo, a biosphere reserve since 1977; the Forest Massif of Odzala-Kokoua was inscribed on the UNESCO World Heritage List in 2023."
     :culture/url "https://en.wikipedia.org/wiki/Odzala-Kokoua_National_Park"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-cog culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "COG"))
                 " COG entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
