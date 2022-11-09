(ns portfolio.layouts.home-page
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  :title "Default homepage"
  [:div
   [:h1 "Heading"]
   [:p [:a {:href "#"} "I am a link"]]
   [:button.button "Click it"]])
