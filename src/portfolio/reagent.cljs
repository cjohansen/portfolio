(ns portfolio.reagent
  (:require [reagent.dom :as rd]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.reagent]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [component el]
     (if el
       (rd/render (if (fn? component)
                    [component]
                    component) el)
       (js/console.error "Asked to render Reagent component without an element")))})

(defn create-scene [scene]
  (cond-> scene
    (:component scene) (update :component #(with-meta % component-impl))
    (:component-fn scene) (update :component-fn
                                  (fn [f]
                                    (fn [data]
                                      (with-meta (f data) component-impl))))))
