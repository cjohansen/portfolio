(ns portfolio.ui.search
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(defn get-ngrams [n s]
  (->> (str/split (str/lower-case s) #"\W")
       (mapcat #(partition n 1 %))
       (map str/join)
       set))

(defn index-scene [{:keys [namespace title docs]}]
  (concat
   (when (string? title)
     (for [ngram (get-ngrams 3 title)]
       [ngram 3]))
   (when (string? namespace)
     (for [ngram (get-ngrams 3 namespace)]
       [ngram 2]))
   (when (string? docs)
     (for [ngram (get-ngrams 3 docs)]
       [ngram 1]))
   (when (string? title)
     (for [ngram (get-ngrams 2 title)]
       [ngram 1]))
   (when (string? namespace)
     (for [ngram (get-ngrams 2 namespace)]
       [ngram 1]))
   (when (string? docs)
     (for [ngram (get-ngrams 2 docs)]
       [ngram 1]))))

(defn add-to-index! [index scene tuples]
  (swap! index
         (fn [idx]
           (reduce (fn [idx [item score]]
                     (update-in idx [item scene] (fnil + 0) score))
                   idx
                   tuples))))

(defn score-hits [xs]
  (->> (group-by first xs)
       (map (fn [[scene xs]]
              [scene (apply + (map second xs))]))))

(defn search [index term]
  (let [res (for [q (str/split term #" ")]
              (let [l (count q)
                    n (min 3 (max 2 l))]
                (->> (get-ngrams n q)
                     (mapcat #(vec (get index %)))
                     score-hits
                     set)))
        relevant (apply set/intersection (map (fn [xs] (set (map first xs))) res))]
    (->> (apply concat res)
         (filter (comp relevant first))
         score-hits
         (sort-by (comp - second)))))

(comment

  (def index (atom {}))

  (add-to-index!
   index
   "portfolio.components.box/shadowed-box"
   (index-scene {:title "Shadowed Box"
                 :namespace "portfolio.components.box/shadowed-box"}))

  (add-to-index!
   index
   "portfolio.components.button/button"
   (index-scene {:title "Button!"
                 :namespace "portfolio.components.button/button"}))

  (add-to-index!
   index
   "portfolio.components.button/aggressive"
   (index-scene {:title "Aggressive button"
                 :namespace "portfolio.components.button/aggressive"}))

  (add-to-index!
   index
   "portfolio.components.button/parameterized"
   (index-scene {:title "Parameterized button"
                 :namespace "portfolio.components.button/parameterized"
                 :docs "A scene demonstrating externalized parameters, that allow Portfolio to subscribe to changes, and offer a UI for manipulating it."}))

  (search @index "change")

  (get-ngrams 3 "Box")
  (get-ngrams 3 "portfolio.components.box/shadowed-box")
  (get-ngrams 3 "portfolio.components.button/button")
  (get-ngrams 3 "portfolio.components.button/aggressive")
  (get-ngrams 3 "portfolio.components.button/parameterized")
  (get-ngrams 3 "portfolio.components.button/stateful")
  (get-ngrams 3 "portfolio.components.button/bomb")

)
