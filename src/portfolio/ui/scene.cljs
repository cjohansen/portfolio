(ns portfolio.ui.scene
  (:require [portfolio.ui.code :as code]
            [portfolio.ui.collection :as collection]))

(defn get-param-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-param* [state scene param]
  (if (map? param)
    (merge param (get-param-overrides state scene))
    param))

(defn get-params [state scene]
  (cond
    (map? (:params scene))
    [(->> (:params scene)
          (map (fn [[k v]] [k (get-param* state scene v)]))
          (into {}))]

    (coll? (:params scene))
    (map #(get-param* state scene %) (:params scene))

    :else
    (:params scene)))

(defn prep-scene-fns [state scenes]
  (for [scene scenes]
    (let [params (get-params state scene)]
      (cond-> (assoc scene :component-params (map code/code-str params))
        (:component scene)
        (assoc :component-fn #(:component scene))

        (:component-fn scene)
        (assoc :component-fn #(apply (:component-fn scene) (concat params [%])))))))

(defn prepare-scenes [state scenes]
  (->> scenes
       (prep-scene-fns state)
       (sort-by :idx)))

(defn get-scene-illustration [state scene selected?]
  {:icon (or (when selected?
               (:selected-icon scene))
             (:icon scene)
             (when selected?
               (collection/get-in-parents state scene :default-scene-selected-icon))
             (collection/get-in-parents state scene :default-scene-icon)
             :ui.icons/bookmark)
   :color (or (when selected?
                (:selected-icon-color scene))
              (:icon-color scene)
              (when-not selected?
                "var(--sidebar-unit-icon-color)"))})

(defn get-scene-atoms [{:keys [params]}]
  (->> (if (map? params) (vals params) params)
       (filter #(satisfies? cljs.core/IWatchable %))))
