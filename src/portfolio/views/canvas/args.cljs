(ns portfolio.views.canvas.args
  (:require [portfolio.protocols :as portfolio]
            [portfolio.components.arguments-panel :refer [ArgumentsPanel]]
            [portfolio.core :as p]
            [clojure.string :as str]))

(def render-impl
  {`portfolio/render-view #'ArgumentsPanel})

(defn prepare-addon-content [panel state location scene]
  (when (:args scene)
    (with-meta
      {:args (let [args (p/get-scene-args state scene)
                   overrides (p/get-scene-args-overrides state scene)]
               (when (map? args)
                 (for [[k v] args]
                   (cond->
                       {:label (str/replace (str k) #"^:" "")
                        :value v
                        :actions [[:set-scene-argument (:id scene) k :event.target/value]]}
                     (k overrides)
                     (assoc :clear-actions [[:remove-scene-argument (:id scene) k]])))))}
      render-impl)))

(def data-impl
  {`portfolio/prepare-addon-content #'prepare-addon-content})

(defn create-args-panel [config]
  (with-meta
    {:id :canvas/args-panel
     :title "Arguments"}
    data-impl))
