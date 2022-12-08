(ns portfolio.views.canvas.param
  (:require [portfolio.components.param-panel :refer [ParamPanel]]
            [portfolio.core :as p]
            [portfolio.view :as view]
            [portfolio.views.canvas.protocols :as canvas]))

(def render-impl
  {`view/render-view #'ParamPanel})

(defn get-input-kind [scene k v]
  (or (get-in scene [:param-defs k :input/kind])
      (cond
        (boolean? v)
        {:kind :boolean
         :value v
         :actions [[:set-scene-param (:id scene) k (not v)]]}

        (number? v)
        {:kind :number
         :value v
         :actions [[:set-scene-param (:id scene) k :event.target/number-value]]}

        :default
        {:kind :text
         :value v
         :actions [[:set-scene-param (:id scene) k :event.target/value]]})))

(defn prepare-param [scene overrides param]
  (when (map? param)
    (for [[k v] param]
      (cond->
          {:label (str k)
           :value v
           :input (get-input-kind scene k v)}
        (= (k param) (k overrides))
        (assoc :clear-actions [[:remove-scene-param (:id scene) k]])))))

(defn prepare-panel-content [panel state scene]
  (when (:param scene)
    (with-meta
      (let [param (p/get-scene-param state scene)
            param (if (satisfies? cljs.core/IWatchable param) @param param)
            overrides (p/get-scene-param-overrides state scene)]
        {:param (prepare-param scene overrides param)})
      render-impl)))

(def data-impl
  {`canvas/prepare-panel-content #'prepare-panel-content})

(defn create-param-panel [config]
  (with-meta
    {:id :canvas/param-panel
     :title "Parameter"}
    data-impl))
