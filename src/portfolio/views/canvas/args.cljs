(ns portfolio.views.canvas.args
  (:require [clojure.string :as str]
            [portfolio.components.arguments-panel :refer [ArgumentsPanel]]
            [portfolio.core :as p]
            [portfolio.view :as view]
            [portfolio.views.canvas.protocols :as canvas]))

(def render-impl
  {`view/render-view #'ArgumentsPanel})

(defn get-input-kind [scene k v]
  (or (get-in scene [:arg-defs k :input/kind])
      (cond
        (boolean? v)
        {:kind :boolean
         :value v
         :actions [[:set-scene-argument (:id scene) k (not v)]]}

        (number? v)
        {:kind :number
         :value v
         :actions [[:set-scene-argument (:id scene) k :event.target/number-value]]}

        :default
        {:kind :text
         :value v
         :actions [[:set-scene-argument (:id scene) k :event.target/value]]})))

(defn prepare-panel-content [panel state scene]
  (when (:args scene)
    (with-meta
      {:args (let [args (p/get-scene-args state scene)
                   args (if (satisfies? cljs.core/IWatchable args) @args args)
                   overrides (p/get-scene-arg-overrides state scene)]
               (when (map? args)
                 (for [[k v] args]
                   (cond->
                       {:label (str/replace (str k) #"^:" "")
                        :value v
                        :input (get-input-kind scene k v)}
                     (= (k args) (k overrides))
                     (assoc :clear-actions [[:remove-scene-argument (:id scene) k]])))))
       :arg-list (when-let [items (->> (:taps state)
                                       (take 15)
                                       (map (fn [v]
                                              {:text (pr-str v)
                                               :actions [[:set-scene-argument (:id scene) v]]}))
                                       seq)]
                   {:title "Taps"
                    :items items})}
      render-impl)))

(def data-impl
  {`canvas/prepare-panel-content #'prepare-panel-content})

(defn create-args-panel [config]
  (with-meta
    {:id :canvas/args-panel
     :title "Arguments"}
    data-impl))
