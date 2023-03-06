(ns portfolio.icons.cross
  "Phosphor icons: https://phosphoricons.com"
  (:require [portfolio.icon :refer-macros [deficon]]))

(deficon icon
  [:svg {:fill "none"
         :viewBox "0 0 256 256"}
   [:line {:x1 "200" :y1 "56" :x2 "56" :y2 "200" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
   [:line {:x1 "200" :y1 "200" :x2 "56" :y2 "56" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]])
