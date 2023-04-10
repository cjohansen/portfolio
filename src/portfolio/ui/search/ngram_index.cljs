(ns portfolio.ui.search.ngram-index
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [portfolio.ui.search.index :as index]))

(defn get-ngrams [s min-n max-n]
  (->> (for [n (range min-n (inc max-n))]
         (->> (str/split (str/lower-case s) #"\W")
              (mapcat #(partition n 1 %))
              (map str/join)))
       (apply concat)
       set))

(defn score-ngram [ngram weight]
  (* weight (count ngram)))

(defn score-text-ngrams [s weight min-n max-n]
  (when (string? s)
    (for [ngram (get-ngrams s min-n max-n)]
      [ngram (score-ngram ngram weight)])))

(defn index-scene [{:keys [collection title docs]}]
  (concat
   (score-text-ngrams title 4 1 3)
   (some-> collection name (score-text-ngrams 3 1 3))
   (score-text-ngrams docs 2 1 3)))

(defn clear-from-index [index id]
  (->> (for [[k v] index]
         [k (dissoc v id)])
       (into {})))

(defn add-to-index! [index id tuples]
  (swap! index
         (fn [idx]
           (reduce (fn [idx [item score]]
                     (update-in idx [item id] (fnil + 0) score))
                   (clear-from-index idx id)
                   tuples))))

(defn score-hits [xs]
  (->> (group-by first xs)
       (map (fn [[scene xs]]
              [scene (apply + (map second xs))]))))

(defn search [index q]
  (when (string? q)
    (let [res (for [q (str/split q #" ")]
                (let [l (count q)
                      n (min 3 (max 2 l))
                      ngrams (get-ngrams q 1 n)
                      hits (->> (get-ngrams q 1 n)
                                (keep #(get index %)))]
                  (when (< (- (count ngrams) (count hits)) 2)
                    (->> (get-ngrams q 1 n)
                         (mapcat #(vec (get index %)))
                         score-hits
                         set))))
          relevant (apply set/intersection (map (fn [xs] (set (map first xs))) res))]
      (->> (apply concat res)
           (filter (comp relevant first))
           score-hits
           (sort-by (comp - second))
           (map (fn [[id score]] {:id id :score score}))))))

(defn create-index [& [{:keys [min-score-ratio]}]]
  (let [index (atom {})
        min-score-ratio (or min-score-ratio 0.4)]
    (reify
      index/Index
      (index [_ document]
        (add-to-index! index (:id document) (index-scene document)))

      (index/query [_ q]
        (let [res (search @index q)
              threshold (* min-score-ratio (:score (first res)))]
          (remove #(< (:score %) threshold) res))))))

(comment

  (def index (atom {}))

  (add-to-index!
   index
   :portfolio.components.box/shadowed-box
   (index-scene {:title "Shadowed Box"
                 :namespace "portfolio.components.box/shadowed-box"}))

  (add-to-index!
   index
   :portfolio.components.button/button
   (index-scene {:title "Button!"
                 :namespace "portfolio.components.button/button"}))

  (add-to-index!
   index
   :portfolio.components.button/aggressive
   (index-scene {:title "Aggressive button"
                 :namespace "portfolio.components.button/aggressive"}))

  (add-to-index!
   index
   :portfolio.components.button/parameterized
   (index-scene {:title "Parameterized button"
                 :namespace "portfolio.components.button/parameterized"
                 :docs "A scene demonstrating externalized parameters, that allow Portfolio to subscribe to changes, and offer a UI for manipulating it."}))

  (search @index "butt")

)

