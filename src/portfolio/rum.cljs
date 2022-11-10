(ns portfolio.rum
  (:require [rum.core :as rum]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.rum]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [wrapper el]
     (if el
       (rum/mount (:component wrapper) el)
       (js/console.error "Asked to render Rum component without an element")))})

(defn create-scene [scene]
  (cond-> scene
    (:component scene) (update :component #(with-meta {:component %} component-impl))
    (:component-fn scene) (update :component-fn
                                  (fn [f]
                                    (fn [data]
                                      (with-meta {:component (f data)} component-impl))))))
