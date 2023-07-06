(ns portfolio.reagent-18
  (:require [reagent.dom.client :as rdc]
            [reagent.impl.template :as reagent]
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
     (assert (some? el) "Asked to render Reagent component without an element")
     (when-let [f (some-> el .-unmount)]
       (when-not (= "react18" (.-unmountLib el))
         (f)))
     (let [root (get-root el)]
       (set! (.-unmount el) (fn []
                              (.unmount root)
                              (set! (.-reactRoot el) nil)
                              (set! (.-innerHTML el) "")
                              (set! (.-unmount el) nil)))
       (set! (.-unmountLib el) "react18")
       (rdc/render root (if (fn? component)
                          [component]
                          component))))})

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
