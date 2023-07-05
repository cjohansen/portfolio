(ns portfolio.ui.scene
  (:require [portfolio.ui.code :as code]))

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

(defn ensure-coll [x]
  (if (and (coll? x) (not (map? x)))
    x
    [x]))

(defn prep-scene-fn [state scene]
  (let [params (get-params state scene)]
    (cond-> (assoc scene :component-params (->> (ensure-coll params)
                                                (keep code/code-str)
                                                seq))
      (:component scene)
      (assoc :component-fn #(:component scene))

      (:component-fn scene)
      (assoc :component-fn #(apply (:component-fn scene) params %&)))))

(defn sort-key [scene]
  [(:line scene) (:idx scene)])

(defn get-scene-atoms [{:keys [params]}]
  (->> (tree-seq coll? identity params)
       (filter #(satisfies? cljs.core/IWatchable %))))
