(ns portfolio.icons.magnifying-glass-plus
  (:require [portfolio.icon :refer-macros [deficon]]))

(deficon icon
  [:svg {:fill "none"
         :viewBox "0 0 256 256"}
   [:line
    {:stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"
     :x1 "84"
     :x2 "148"
     :y1 "116"
     :y2 "116"}]
   [:line
    {:stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"
     :x1 "116"
     :x2 "116"
     :y1 "84"
     :y2 "148"}]
   [:circle
    {:cx "116"
     :cy "116"
     :r "84"
     :stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"}]
   [:line
    {:stroke "currentColor"
     :stroke-linecap "round"
     :stroke-linejoin "round"
     :stroke-width "16"
     :x1 "175.4"
     :x2 "224"
     :y1 "175.4"
     :y2 "224"}]])
