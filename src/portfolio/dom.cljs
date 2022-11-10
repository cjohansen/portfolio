(ns portfolio.dom
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.dom]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [component el]
     (if el
       (do
         (.forEach
          (js/Object.keys (.-dataset el))
          (fn [k]
            (aset (.-dataset el) k "")))
         (set! (.-innerHTML el) "")
         (.appendChild el (.cloneNode (:element component) true)))
       (js/console.error "Asked to render DOM element without a container")))})

(defn create-scene [scene]
  (cond-> scene
    (:component scene) (update :component #(with-meta {:element %} component-impl))
    (:component-fn scene) (update :component-fn
                                  (fn [f]
                                    (fn [data]
                                      (with-meta {:element (f data)} component-impl))))))
