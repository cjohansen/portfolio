(ns portfolio.ui.search.pliable-index
  "Pliable Index is a way too short and over-simplified implementation of some
  concepts loosely borrowed from Elastic Search. It works on an in-memory index
  represented by the map, and may be suitable to power searches in client-side
  datasets that aren't big enough to require the bells and whistles of a more
  tuned implementation.

  Pliable index provides `index-document` for indexing documents according to a
  schema, and `query` to query the resulting index using one or more criteria.
  Refer to these functions for a more detailed explanation.

  Indexing a document consists of breaking its content into tokens and storing
  them in named indexes. Each named sub-index can use a different stack of
  tokenizers. When querying, you can tokenize the query using the same tools,
  combine different indexes with logical AND/OR, and apply boosts.

  Pliable Index has tokenizers for keywords, words in strings, stripping
  diacritics, as well as ngrams and edge ngrams. See individual functions for
  details."
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def sep-re #"[/\.,_\-\?!\s\n\r\(\)\[\]:]+")

(defn tokenize-lower-case
  "Converts a string to a single lower case token"
  [s]
  [(str/lower-case (str/trim s))])

(defn remove-diacritics
  "Converts a string to a single token with all combining diacritis removed: é
  becomes e, å becomes a, etc."
  [s]
  [(-> (str/trim s)
       (.normalize "NFD")
       (str/replace #"[\u0300-\u036f]" "")
       str/lower-case)])

(defn tokenize-words
  "Converts a string to a sequence of word tokens, removing punctuation."
  [s]
  (filter not-empty (str/split s sep-re)))

(defn tokenize-keyword
  "Converts a keyword to tokens with and without keyword punctuation. Passes
  strings through as a single token."
  [x]
  (if (keyword? x)
    (if-let [ns (namespace x)]
      (let [s (str ns "/" (name x))]
        [ns (name x) s (str x)])
      [(str x) (name x)])
    [x]))

(defn stringify-keyword
  "Converts keywords to a string token, punctuation intact. Passes strings through
  as a single token."
  [x]
  [(cond-> x
     (keyword? x) str)])

(defn tokenize-ngrams
  "Converts a string to ngram tokens. When only one number is passed, only that
  sized ngrams are produced, otherwise, every length ngram from `min-n` to
  `max-n` is produced.

  ```clj
  (tokenize-ngrams 1 2 \"Hello\") ;;=> (\"H\" \"e\" \"l\" \"l\" \"o\"
                                  ;;    \"He\" \"el\" \"ll\" \"lo\")
  ```"
  ([n word]
   (tokenize-ngrams n n word))
  ([min-n max-n word]
   (->> (for [n (range min-n (inc max-n))]
          (->> word
               (partition n 1)
               (map str/join)))
        (apply concat))))

(defn tokenize-edge-ngrams
  "Converts a string to ngram tokens from the beginning of the string.
  When only one number is passed, only that sized ngrams are produced,
  otherwise, every length ngram from `min-n` to `max-n` is produced.

  ```clj
  (tokenize-edge-ngrams 1 5 \"Hello\") ;;=> (\"H\" \"He\" \"Hel\" \"Hell\" \"Hello\")
  ```"
  ([n word]
   (tokenize-edge-ngrams n n word))
  ([min-n max-n word]
   (for [n (range min-n (inc max-n))]
     (str/join (take n word)))))

(defn tokenize
  "Converts value `x` to tokens with the provided `tokenizers`. `tokenizers` is a
  seq of functions that take a single value and return a seq of tokens. The type
  of value `x` and the produced tokens are arbitrary and up to the user, but
  tokenizers must compose. Built-in tokenizers mostly only work with strings for
  `x` (some accept keywords) and all produce a sequence of strings."
  [x & [tokenizers]]
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

(defn index-document
  "Index data in `doc` according to `schema` under `id` in `index`. Returns the
  updated index. At its simplest, the schema only specifies which keys in `doc`
  to include in the index:

  ```clj
  {:title {}
   :description {}}
  ```

  This schema will use the `default-tokenizers` to index `:title` and
  `:description` in `:doc`. The following schema is the explicit equivalent. It
  names what function `:f` to apply to `doc` to extract the data to index, and
  what `:tokenizers` to use. The keys of the schema name the resulting field
  indexes - when querying you can choose to query across all fields, or name
  individual fields to query:

  ```clj
  {:title
   {:f :title
    :tokenizers [stringify-keyword
                 remove-diacritics
                 tokenize-words]}

   :description
   {:f :description
    :tokenizers [stringify-keyword
                 remove-diacritics
                 tokenize-words]}}
  ```

  You can use schemas to index the same fields multiple times with different
  tokenizers:

  ```clj
  {:title
   {:f :title
    :tokenizers [remove-diacritics
                 tokenize-words]}

   :title.ngrams
   {:f :title
    :tokenizers [remove-diacritics
                 tokenize-words
                 (partial tokenize-ngrams 3)]}}
  ```"
  [index schema id doc]
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

(defn query
  "Query the index created by `index-document` with `q`. `q` is a map with two
  keys:

  - `:queries` A seq of maps defining a query (see below)
  - `:operator` Either `:or` or `:and` (default)

  Each query in `:queries` is a map of the following keys:

  - `:q` The query string
  - `:tokenizers` How to tokenize the query string before matching against
                  indexes. Defaults to `default-tokenizers`.
  - `:fields` What field indexes to match against. Defaults to all fields.
  - `:boost` A score boost for this query.
  - `:operator` Either `:or` or `:and` (default). Determines whether a
                single token match is good enough (`:or`), or if all tokens must
                match (`:and`).
  - `:min-accuracy` When `:operator` is `:or`, this can be a number between `0`
                    and `1` determining the lowest acceptable success rate. `0.5`
                    means that at least half the tokens from `q` must match tokens
                    in the queried indexes

  Each query will possibly find some results. Results scored based on the number
  of matching tokens. These scores are then boosted for each individual query.
  The final result will be either the intersection of all sub-results (`:and`),
  or the union (`:or`). The final score for each document id will be calculated
  by summarizing individual query scores, and `query` returns a sorted seq of
  results, with the best scoring result first.

  Results are maps of:

  - `:id` The id of the document
  - `:score` The calculated total score
  - `:fields` A map of `{field score}` - e.g. what fields contributed to the
              result, and their individual scores.
  - `:terms` A map of `{term score}` - e.g. what terms contributed to the result,
             and their individual scores."
  [index q]
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
