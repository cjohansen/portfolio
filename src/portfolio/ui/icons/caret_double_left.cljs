(ns portfolio.ui.icons.caret-double-left
  "Phosphor icons: https://phosphoricons.com"
  (:require [portfolio.ui.icon :refer-macros [deficon]]))

(deficon icon
  [:svg {:fill "none"
         :viewBox "0 0 256 256"}
   [:rect {:height "256"
           :width "256"}]
   [:polyline {:points "200 208 120 128 200 48"
               :stroke "currentColor"
               :stroke-linecap "round"
               :stroke-linejoin "round"
               :stroke-width "16"}]
   [:polyline {:points "120 208 40 128 120 48"
               :stroke "currentColor"
               :stroke-linecap "round"
               :stroke-linejoin "round"
               :stroke-width "16"}]])
