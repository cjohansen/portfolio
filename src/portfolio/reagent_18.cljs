(ns portfolio.reagent-18
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [reagent.dom.client :as rdc]
            [reagent.impl.template :as reagent])
  (:require-macros [portfolio.reagent-18]))

::data/keep

(def ^:dynamic *decorator* nil)

(defn set-decorator! [decorator]
  (set! *decorator* decorator))

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
     (let [root (get-root el)
           decorator (or *decorator* identity)]
       (set! (.-unmount el) (fn []
                              (.unmount root)
                              (set! (.-reactRoot el) nil)
                              (set! (.-innerHTML el) "")
                              (set! (.-unmount el) nil)))
       (set! (.-unmountLib el) "react18")
       (rdc/render root
                   [decorator
                    (if (fn? component)
                      [component]
                      component)])))})

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
