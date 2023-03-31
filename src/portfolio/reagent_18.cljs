(ns portfolio.reagent-18
  (:require [reagent.dom.client :as rdc]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.reagent-18]))

::data/keep

(defn get-root [el]
  (when-not (.-reactRoot el)
    (set! (.-reactRoot el) (rdc/create-root el)))
  (.-reactRoot el))

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (if el
       (rdc/render (get-root el) (if (fn? component)
                                   [component]
                                   component))
       (js/console.error "Asked to render Reagent component without an element")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
