(ns portfolio.reagent
  (:require [reagent.dom :as rd]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.reagent]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (when-let [f (some-> el .-unmount)]
       (f))
     (if el
       (rd/render (if (fn? component)
                    [component]
                    component) el)
       (js/console.error "Asked to render Reagent component without an element")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
