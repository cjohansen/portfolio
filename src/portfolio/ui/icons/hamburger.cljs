(ns portfolio.ui.icons.hamburger
  "Phosphor icons: https://phosphoricons.com"
  (:require [portfolio.ui.icon :refer-macros [deficon]]))

(deficon icon
  [:svg {:viewBox "0 0 256 256"}
   [:path
    {:d "M48.8,96A8,8,0,0,1,41,86.3C47.4,55.5,83.9,32,128,32s80.6,23.5,87,54.3a8,8,0,0,1-7.8,9.7Z"
     :fill "none"
     :stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"}]
   [:path
    {:d "M208,168v16a32,32,0,0,1-32,32H80a32,32,0,0,1-32-32V168"
     :fill "none"
     :stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"}]
   [:polyline
    {:fill "none"
     :points "28 176 68 160 108 176 148 160 188 176 228 160"
     :stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"}]
   [:line
    {:fill "none"
     :stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"
     :x1 "24"
     :x2 "232"
     :y1 "128"
     :y2 "128"}]])
