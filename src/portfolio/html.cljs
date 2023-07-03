(ns portfolio.html
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.html]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (when-let [f (some-> el .-unmount)]
       (f))
     (.forEach
      (js/Object.keys (.-dataset el))
      (fn [k]
        (aset (.-dataset el) k "")))
     (if el
       (set! (.-innerHTML el) component)
       (js/console.error "Asked to render HTML string without an element")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))

(data/register-scene-renderer!
 (fn [x]
   (when-let [scene (cond
                      (string? x)
                      {:component x}

                      (string? (:component x))
                      x)]
     (create-scene scene))))
