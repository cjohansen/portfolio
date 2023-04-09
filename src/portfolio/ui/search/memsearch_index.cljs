(ns portfolio.ui.search.memsearch-index
  (:require [memsearch.core :as ms]
            [portfolio.ui.search.index :as index]
            [clojure.string :as str]))

(defn build-doc-index [doc]
  (ms/text-index
   [{:id (:id doc)
     :content (->> [(:title doc)
                    (:docs doc)
                    (some-> doc :collection name)]
                   (remove nil?)
                   (str/join " "))}]))

(defn create-index []
  (let [index (atom {})]
    (reify
      index/Index
      (index-document [_self doc]
        (swap! index #(merge-with concat % (build-doc-index doc))))

      (search [_self q]
        (->> (ms/text-search q @index)
             (map (fn [[id {:keys [score]}]]
                    {:id id
                     :score score}))
             (sort-by (comp - :score)))))))
