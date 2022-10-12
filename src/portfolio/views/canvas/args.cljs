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

(defn prepare-taps [scene overrides taps]
  (when-let [items (seq (take 15 taps))]
    {:title "Taps"
     :items
     (->> items
          (map (fn [v]
                 (let [selected? (= v overrides)]
                   {:text (pr-str v)
                    :selected? selected?
                    :actions [(if selected?
                                [:remove-scene-argument (:id scene)]
                                [:set-scene-argument (:id scene) v])]}))))}))

(defn prepare-arguments [scene overrides args]
  (when (map? args)
    (for [[k v] args]
      (cond->
          {:label (str/replace (str k) #"^:" "")
           :value v
           :input (get-input-kind scene k v)}
        (= (k args) (k overrides))
        (assoc :clear-actions [[:remove-scene-argument (:id scene) k]])))))

(defn prepare-panel-content [panel state scene]
  (when (:args scene)
    (with-meta
      (let [args (p/get-scene-args state scene)
            args (if (satisfies? cljs.core/IWatchable args) @args args)
            overrides (p/get-scene-arg-overrides state scene)]
        {:args (prepare-arguments scene overrides args)
         :arg-list (prepare-taps scene overrides (:taps state))})
      render-impl)))

(def data-impl
  {`canvas/prepare-panel-content #'prepare-panel-content})

(defn create-args-panel [config]
  (with-meta
    {:id :canvas/args-panel
     :title "Arguments"}
    data-impl))
