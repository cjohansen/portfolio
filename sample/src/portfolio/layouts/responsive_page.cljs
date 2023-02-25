(ns portfolio.layouts.responsive-page
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene responsive-page
  :title "Responsive page"
  [param opt]
  [:div
   [:h1 "This is a responsive page."]
   [:p "It reads Portfolio's viewport config to determine render modus"]
   [:p
    (if (< (:viewport/width opt 800) 800)
      "This is the mobile mode"
      "This is the desktop mode")]])
