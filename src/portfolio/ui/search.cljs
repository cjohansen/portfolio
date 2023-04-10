(ns portfolio.ui.search
  (:require [portfolio.ui.search.index :as index]
            [portfolio.ui.search.pliable-index :as pliable]))

(defn get-diff-keys [m1 m2]
  (->> m1
       (filter (fn [[k v]] (not= (m2 k) v)))
       (map first)))

(defn get-diffables [f xs]
  (->> xs
       (map (fn [[k v]]
              [k (if (ifn? f) (f v) v)]))
       (into {})))

(defn indexable? [x]
  (and (not (fn? x))
       (not (satisfies? cljs.core/IWatchable x))))

(defn ->indexable [x]
  (cond
    (map? x)
    (->> x
         (filter (comp indexable? second))
         (into {}))

    (coll? x)
    (filter indexable? x)

    :else
    (if (indexable? x)
      x
      nil)))

(defn get-indexable-data [x]
  (let [res (->> (dissoc x :updated-at :line :idx :component :component-fn)
                 ->indexable)]
    (cond
      (map? (:params res))
      (update res :params ->indexable)

      (coll? (:params res))
      (update res :params ->indexable)

      :else
      res)))

(defn get-params-data [scene]
  (->> (:params scene)
       (tree-seq coll? identity)
       (filter #(or (string? %) (keyword? %)))))

(defn index-scene [index scene]
  (let [ngram-tokenizers [pliable/stringify-keyword
                          pliable/remove-diacritics
                          pliable/tokenize-words
                          (partial pliable/tokenize-ngrams 2 3)]]
    (pliable/index-document
     index
     {:title {}
      :title.ngram {:f :title
                    :tokenizers ngram-tokenizers}
      :docs {}
      :docs.ngram {:f :docs
                   :tokenizers ngram-tokenizers}
      :tags {}
      :collection {}
      :params-data {:f get-params-data}}
     (:id scene)
     scene)))

(defn search [index q]
  (pliable/query
   index
   {:operator :or
    :queries
    [{:q q
      :operator :and
      :boost 3}
     {:q q
      :tokenizers [pliable/remove-diacritics
                   pliable/tokenize-words
                   (partial pliable/tokenize-ngrams 2 3)]
      :fields #{:title.ngram :docs.ngram}
      :operator :or
      :min-accuracy 0.3}]}))

(defn create-index []
  (let [index (atom {})]
    (reify
      index/Index
      (index [_ doc]
        (swap! index index-scene doc))

      (query [_ q]
        (search @index q)))))
