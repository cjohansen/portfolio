(ns portfolio.dumdom
  (:require [dumdom.core :as d]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.dumdom]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component id updated-at]} el]
     (assert (some? el) "Asked to render Dumdom component without an element.")
     (when-let [f (some-> el .-unmount)]
       (when-not (= "dumdom" (.-unmountLib el))
         (f)))
     (set! (.-unmount el) (fn []
                            (.forEach
                             (js/Object.keys (.-dataset el))
                             (fn [k]
                               (aset (.-dataset el) k "")))))
     (set! (.-unmountLib el) "dumdom")
     (d/render [:div {:key (str id "-" updated-at)} component] el))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
