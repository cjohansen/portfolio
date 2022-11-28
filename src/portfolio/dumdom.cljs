(ns portfolio.dumdom
  (:require [dumdom.core :as d]
            [portfolio.adapter :as adapter]
            [portfolio.data :as data])
  (:require-macros [portfolio.dumdom]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component id updated-at]} el]
     (if el
       (d/render [:div {:key (str id "-" updated-at)} component] el)
       (js/console.error "Asked to render Dumdom component without an element")))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
