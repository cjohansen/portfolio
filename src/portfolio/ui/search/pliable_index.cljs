(ns portfolio.ui.search.pliable-index
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [portfolio.ui.search.index :as index]))

(def sep-re #"[/\.,_\-\?!\s\n\r\(\)\[\]]+")

(defn tokenize-lc [s]
  (cond
    (string? s)
    (-> s
        str/trim
        str/lower-case
        (str/split sep-re))

    (and (coll? s) (not (map? s)))
    (mapcat tokenize-lc s)))

(defn tokenize-clean [s]
  (cond
    (string? s)
    (-> s
        str/trim
        (.normalize "NFD")
        (str/replace #"[\u0300-\u036f]" "")
        str/lower-case
        (str/split sep-re))

    (and (coll? s) (not (map? s)))
    (mapcat tokenize-clean s)))

(defn get-ngrams
  ([word n]
   (get-ngrams word n n))
  ([word min-n max-n]
   (->> (for [n (range min-n (inc max-n))]
          (->> word
               (partition n 1)
               (map str/join)))
        (apply concat))))

(defn get-clean-ngrams [x min-n max-n]
  (mapcat #(get-ngrams % min-n max-n) (tokenize-clean x)))

(defn get-words [x tokenize]
  (cond
    (keyword? x)
    (if-let [ns (namespace x)]
      (let [s (str ns "/" (name x))]
        (concat [s (str x)]
                (get-words s tokenize)))
      (concat [(str x)]
              (get-words (name x) tokenize)))

    (string? x)
    (tokenize x)

    (and (coll? x) (not (map? x)))
    (mapcat #(get-words % tokenize) x)))

(defn get-field-syms [field xs]
  (for [[word weight] (into [] (frequencies xs))]
    {:field field :sym word :weight weight}))

(defn index-document [index schema id doc]
  (->> schema
       (mapcat (fn [[field config]]
                 (let [f (:f config field)
                       tokenize (or (:tokenizer config) tokenize-clean)]
                   (cond
                     (:ngrams config)
                     (let [[n1 n2] (:ngrams config)]
                       (->> (tokenize (f doc))
                            (mapcat #(get-ngrams % n1 n2))
                            (get-field-syms field)))

                     :else
                     (->> (get-words (f doc) tokenize)
                          (get-field-syms field))))))
       (reduce (fn [index {:keys [field sym weight]}]
                 (assoc-in index [field sym id] {:weight weight}))
               index)))

(defn score-term [index fields term]
  (->> fields
       (mapcat (fn [field]
                 (for [[id {:keys [weight]}] (get-in index [field term])]
                   {:id id
                    :score weight
                    :field field})))
       (group-by :id)
       (map (fn [[id xs]]
              {:id id
               :score (reduce + 0 (map :score xs))
               :fields (into {} (map (juxt :field :score) xs))
               :term term}))))

(defn match-query [index {:keys [q boost tokenizer fields operator]}]
  (let [tokenize (or tokenizer tokenize-clean)
        fields (or fields (keys index))
        boost (or boost 1)
        terms (tokenize q)]
    (->> terms
         (mapcat #(score-term index fields %))
         (group-by :id)
         (filter (fn [[_ xs]]
                   (or (= :or operator)
                       (= (count xs) (count terms)))))
         (map (fn [[id xs]]
                {:id id
                 :score (* boost (reduce + 0 (map :score xs)))
                 :fields (->> (for [[k score] (apply merge-with + (map :fields xs))]
                                [k (* boost score)])
                              (into {}))
                 :terms (->> xs
                             (map (juxt :term (comp #(* boost %) :score)))
                             (into {}))})))))

(defn search-index [index q]
  (let [res (map #(match-query index %) (:queries q))
        ids (map #(set (map :id %)) res)
        res-ids (if (= :or (:operator q))
                  (apply set/union ids)
                  (apply set/intersection ids))]
    (->> (apply concat res)
         (filter (comp res-ids :id))
         (group-by :id)
         (map (fn [[id xs]]
                {:id id
                 :score (reduce + 0 (map :score xs))
                 :fields (apply merge-with + (map :fields xs))
                 :terms (apply merge-with + (map :terms xs))})))))

(defn get-params-data [scene]
  (->> (:params scene)
       (tree-seq coll? identity)
       (filter #(or (string? %) (keyword? %)))))

(defn index-scene [index scene]
  (index-document
   index
   {:title {}
    :title.ngram {:f :title
                  :ngrams [3 4]}
    :docs {}
    :docs.ngram {:f :docs
                 :ngrams [3 4]}
    :tags {}
    :collection {}
    :params-data {:f get-params-data}}
   (:id scene)
   scene))

(defn search [index q]
  (when (not-empty (some-> q str/trim))
    (search-index
     index
     {:operator :or
      :queries
      [{:q q
        :operator :and
        :boost 3}
       {:q q
        :tokenizer #(get-clean-ngrams % 3 4)
        :operator :and}]})))

(defn create-index []
  (let [index (atom {})]
    (reify
      index/Index
      (index-document [_ doc]
        (swap! index index-scene doc))

      (search [_ q]
        (search @index q)))))
