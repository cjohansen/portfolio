(ns portfolio.ui.scene
  (:require [clojure.walk :as walk]
            [portfolio.ui.code :as code]))

(defn atom? [x]
  (satisfies? cljs.core/IWatchable x))

(defn get-param-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-param* [state scene param]
  (if (map? param)
    (merge param (get-param-overrides state scene))
    param))

(defn get-params [state scene]
  (cond
    (map? (:params scene))
    (->> (:params scene)
         (map (fn [[k v]] [k (get-param* state scene v)]))
         (into {}))

    (coll? (:params scene))
    (map #(get-param* state scene %) (:params scene))

    :else
    (get-param* state scene (:params scene))))

(defn get-param-data [params]
  (walk/postwalk
   (fn [x]
     (if (atom? x)
       (deref x)
       x))
   params))

(defn prep-scene-fn [state scene]
  (let [params (get-params state scene)]
    (cond-> (assoc scene
                   :component-params (code/code-str params)
                   :rendered-data {:params (get-param-data params)
                                   :id (:id scene)
                                   :reloaded-at (or (:portfolio.ui/reloaded-at state) 0)
                                   :updated-at (:updated-at scene)})
      (:component scene)
      (assoc :component-fn #(:component scene))

      (:component-fn scene)
      (assoc :component-fn #(apply (:component-fn scene) params %&)))))

(defn sort-key [scene]
  [(:line scene) (:idx scene)])

(defn get-scene-atoms [{:keys [params]}]
  (->> (tree-seq coll? identity params)
       (filter atom?)))
