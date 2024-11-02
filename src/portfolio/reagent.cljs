(ns portfolio.reagent
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [reagent.dom :as rd]
            [reagent.impl.template :as reagent])
  (:require-macros [portfolio.reagent]))

::data/keep

(def ^:dynamic *decorator* nil)

(defn set-decorator! [decorator]
  (set! *decorator* decorator))

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (when-let [f (some-> el .-unmount)]
       (when-not (= "reagent" (.-unmountLib el))
         (f)))
     (if el
       (do
         (rd/render [(or *decorator*
                         identity)
                     (if (fn? component)
                       [component]
                       component)] el)
         (set! (.-unmountLib el) "reagent")
         (set! (.-unmount el)
               (fn []
                 (rd/unmount-component-at-node el)
                 (set! (.-unmount el) nil))))
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
