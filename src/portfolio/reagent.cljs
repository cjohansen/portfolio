(ns portfolio.reagent
  (:require [reagent.dom :as rd]
            [reagent.impl.template :as reagent]
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

(data/register-scene-renderer!
 (fn [x]
   (when-let [scene (cond
                      (reagent/valid-tag? x)
                      {:component x}

                      (reagent/valid-tag? (:component x))
                      x)]
     (create-scene scene))))
