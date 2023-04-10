(ns portfolio.ui.search.pliable-index
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def sep-re #"[/\.,_\-\?!\s\n\r\(\)\[\]:]+")

(defn tokenize-lower-case [s]
  [(str/lower-case (str/trim s))])

(defn remove-diacritics [s]
  [(-> (str/trim s)
       (.normalize "NFD")
       (str/replace #"[\u0300-\u036f]" "")
       str/lower-case)])

(defn tokenize-words [s]
  (filter not-empty (str/split s sep-re)))

(defn tokenize-keyword [x]
  (if (keyword? x)
    (if-let [ns (namespace x)]
      (let [s (str ns "/" (name x))]
        [ns (name x) s (str x)])
      [(str x) (name x)])
    [x]))

(defn stringify-keyword [x]
  [(cond-> x
     (keyword? x) str)])

(defn tokenize-ngrams
  ([n word]
   (tokenize-ngrams n n word))
  ([min-n max-n word]
   (->> (for [n (range min-n (inc max-n))]
          (->> word
               (partition n 1)
               (map str/join)))
        (apply concat))))

(defn tokenize [x & [tokenizers]]
  (reduce
   (fn [tokens f] (mapcat f tokens))
   (remove nil? (if (coll? x) x [x]))
   (or tokenizers [vector])))

(def default-tokenizers
  [stringify-keyword
   remove-diacritics
   tokenize-words])

(defn get-field-syms [field xs]
  (for [[word weight] (into [] (frequencies xs))]
    {:field field :sym word :weight weight}))

(defn index-document [index schema id doc]
  (->> schema
       (mapcat (fn [[field config]]
                 (let [f (:f config field)]
                   (->> (tokenize (f doc) (or (:tokenizers config) default-tokenizers))
                        (get-field-syms field)))))
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

(defn qualified-match? [terms res {:keys [operator min-accuracy]}]
  (<= (cond
        (and (= :or operator) min-accuracy)
        (* min-accuracy (count terms))

        (= :or operator)
        1

        :else (count terms))
      (count res)))

(defn match-query [index {:keys [q boost tokenizers fields] :as query}]
  (let [fields (or fields (keys index))
        boost (or boost 1)
        terms (tokenize q (or tokenizers default-tokenizers))]
    (->> terms
         (mapcat #(score-term index fields %))
         (group-by :id)
         (filter (fn [[_ xs]]
                   (qualified-match? terms xs query)))
         (map (fn [[id xs]]
                {:id id
                 :score (* boost (reduce + 0 (map :score xs)))
                 :fields (->> (for [[k score] (apply merge-with + (map :fields xs))]
                                [k (* boost score)])
                              (into {}))
                 :terms (->> xs
                             (map (juxt :term (comp #(* boost %) :score)))
                             (into {}))})))))

(defn query [index q]
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
