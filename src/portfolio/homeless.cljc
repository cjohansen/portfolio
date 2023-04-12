(ns portfolio.homeless
  (:require [clojure.string :as str]))

(defn debounce [f ms]
  #?(:cljs
     (let [timer (atom nil)]
       (fn [& args]
         (some-> @timer js/clearTimeout)
         (reset! timer (js/setTimeout #(apply f args) ms))))
     :clj (fn [& args] (apply f args))))

(defn get-words [s]
  (when (not-empty s)
    (str/split s #"[\- ]+")))

(defn ->title [s]
  (str/capitalize (str/join " " (get-words s))))

(defn title-case [s]
  (->> (get-words s)
       (map str/capitalize)
       (str/join " ")))
