(ns portfolio.dom
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.dom]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (if el
       (do
         (.forEach
          (js/Object.keys (.-dataset el))
          (fn [k]
            (aset (.-dataset el) k "")))
         (set! (.-innerHTML el) "")
         (.appendChild el (.cloneNode component true)))
       (js/console.error "Asked to render DOM element without a container")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
