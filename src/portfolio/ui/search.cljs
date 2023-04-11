(ns portfolio.ui.search
  (:require [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.search.pliable-index :as pliable]
            [portfolio.ui.search.protocols :as index]))

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

(def ngram-tokenizers
  [pliable/stringify-keyword
   pliable/remove-diacritics
   pliable/tokenize-words
   (partial pliable/tokenize-ngrams 2 3)])

(def prefix-tokenizers
  [pliable/stringify-keyword
   pliable/remove-diacritics
   pliable/tokenize-words
   (partial pliable/tokenize-edge-ngrams 2 10)])

(defn index-scene [index scene]
  (pliable/index-document
   index
   {:title {}
    :title.ngram {:f :title
                  :tokenizers ngram-tokenizers}
    :title.prefix {:f :title
                   :tokenizers prefix-tokenizers}
    :docs {}
    :docs.ngram {:f :docs
                 :tokenizers ngram-tokenizers}
    :tags {}
    :collection {}
    :params-data {:f get-params-data}}
   (:id scene)
   scene))

(defn search [index q]
  (pliable/query
   index
   {:operator :or
    :queries
    [{:q q
      :operator :and
      :boost 3}
     {:q q
      :operator :and
      :tokenizers prefix-tokenizers
      :fields #{:title.prefix}
      :boost 2}
     {:q q
      :tokenizers ngram-tokenizers
      :fields #{:title.ngram :docs.ngram}
      :operator :or
      :min-accuracy 0.5}]}))

(defn create-index []
  (let [index (atom {})]
    (reify
      index/Index
      (index [_ doc]
        (swap! index index-scene doc))

      (query [_ q]
        (search @index q)))))

(defn prepare-result [state location result]
  (let [doc (collection/by-id state (:id result))]
    {:title (:title doc)
     :illustration (collection/get-illustration doc state)
     :actions [[:go-to-location (routes/get-location location doc)]]}))

(defn prepare-search [state location]
  (let [q (not-empty (:search/query state))]
    {:icon :portfolio.ui.icons/magnifying-glass
     :placeholder "Search"
     :text (:search/query state)
     :on-input (->> [[:assoc-in [:search/query] :event.target/value]
                     [:search :event.target/value]]
                    (remove nil?))
     :action (when q
               {:icon :portfolio.ui.icons/x
                :actions [[:assoc-in [:search/query] ""]
                          [:assoc-in [:search/suggestions] nil]]})
     :suggestions (for [result (take 6 (:search/suggestions state))]
                    (if (satisfies? index/SearchResult (:index state))
                      (index/prepare-result (:index state) state location result)
                      (prepare-result state location result)))}))
