(ns portfolio.scene
  (:require [cljs.env :as env]))

(defn portfolio-active? []
  (if-let [options (and cljs.env/*compiler*
                        (:options @cljs.env/*compiler*))]
    (cond
      (false? (:portfolio/enabled? options)) false
      (false? (get-in options [:closure-defines "portfolio.core/enabled"])) false
      :else true)
    true))

(defn get-options-map [id syms]
  (let [pairs (partition-all 2 syms)
        rest (apply concat (drop-while (comp keyword? first) pairs))]
    (->> pairs
         (take-while (comp keyword? first))
         (map vec)
         (into (cond-> {:id (keyword (str *ns*) (str id))}
                 (= 1 (count rest)) (assoc :component-fn `(fn [_#] ~(first rest)))
                 (< 1 (count rest)) (assoc :component-fn `(fn ~(first rest)
                                                            ~@(drop 1 rest))))))))
