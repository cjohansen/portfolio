(ns portfolio.scene)

(defn get-options-map [id syms]
  (let [pairs (partition-all 2 syms)
        rest (apply concat (drop-while (comp keyword? first) pairs))]
    (->> pairs
         (take-while (comp keyword? first))
         (map vec)
         (into (cond-> {:id (keyword (str *ns*) (str id))}
                 (= 1 (count rest)) (assoc :component (first rest))
                 (< 1 (count rest)) (assoc :component-fn `(fn ~(first rest)
                                                            ~@(drop 1 rest))))))))
