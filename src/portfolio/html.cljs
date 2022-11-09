(ns portfolio.html
  (:require [portfolio.adapter :as adapter])
  (:require-macros [portfolio.html]))

(def component-impl
  {`adapter/render-component
   (fn [component el]
     (.forEach
      (js/Object.keys (.-dataset el))
      (fn [k]
        (aset (.-dataset el) k "")))
     (if el
       (set! (.-innerHTML el) (:html component))
       (js/console.error "Asked to render HTML string without an element")))})

(defn create-scene [scene]
  (cond-> scene
    (:component scene) (update :component #(with-meta {:html %} component-impl))
    (:component-fn scene) (update :component-fn
                                  (fn [f]
                                    (fn [data]
                                      (with-meta {:html (f data)} component-impl))))))
