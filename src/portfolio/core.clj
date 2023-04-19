(ns portfolio.core
  (:require [cljs.env :as env]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(defn portfolio-active? []
  (if-let [options (and cljs.env/*compiler*
                        (:options @cljs.env/*compiler*))]
    (cond
      (false? (:portfolio/enabled? options)) false
      (false? (get-in options [:closure-defines "portfolio.core/enabled"])) false
      :else true)
    true))

(defn function-like? [f]
  (or (symbol? f)
      (and (list? f) (= 'var (first f)))))

(defn get-code-str [syms]
  (-> (with-out-str (apply pprint/pprint syms))
      str/trim
      (str/replace #"let\n" "let")))

(defn get-options-map [id line syms]
  (let [docs (when (string? (first syms)) (first syms))
        pairs (partition-all 2 (drop (if docs 1 0) syms))
        rest (apply concat (drop-while (comp keyword? first) pairs))
        fn-like? (function-like? (first rest))]
    (->> pairs
         (take-while (comp keyword? first))
         (map vec)
         (into (cond-> {:id (keyword (str *ns*) (str id))
                        :line line
                        :docs docs}
                 (and (= 1 (count rest)) fn-like?)
                 (assoc :component-fn (first rest))

                 (and (= 1 (count rest)) (not fn-like?))
                 (assoc :component-fn `(fn [& _#]
                                         ~(first rest))
                        :code (get-code-str rest))

                 (< 1 (count rest))
                 (assoc :component-fn `(fn ~(cond-> (first rest)
                                              (< (count (first rest)) 2)
                                              (into ['& 'args]))
                                         ~@(drop 1 rest))
                        :code (get-code-str (next rest))))))))

(defn get-collection-options [syms]
  (let [docs (when (string? (first syms)) (first syms))
        opts (if docs
               (assoc (second syms) :docs docs)
               (first syms))]
    [(keyword (str *ns*)) opts]))
