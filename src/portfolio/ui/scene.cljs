(ns portfolio.ui.scene
  (:require [portfolio.ui.code :as code]))

(defn get-param-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-param [state scene]
  (if (map? (:param scene))
    (merge (:param scene) (get-param-overrides state scene))
    (:param scene)))

(defn prep-scene-fns [state scenes]
  (for [scene scenes]
    (let [param (get-param state scene)]
      (cond-> (assoc scene :component-param (code/code-str param))
        (:component scene)
        (assoc :component-fn #(:component scene))

        (:component-fn scene)
        (assoc :component-fn #((:component-fn scene) param %))))))

(defn prepare-scenes [state scenes]
  (->> scenes
       (prep-scene-fns state)
       (sort-by :idx)))
