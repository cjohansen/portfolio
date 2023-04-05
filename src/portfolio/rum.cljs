(ns portfolio.rum
  (:require [rum.core :as rum]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.rum]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (when-let [f (some-> el .-unmount)]
       (f))
     (if el
       (rum/mount component el)
       (js/console.error "Asked to render Rum component without an element")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
