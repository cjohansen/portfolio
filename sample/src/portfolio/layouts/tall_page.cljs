(ns portfolio.layouts.tall-page
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  :title "100vh page"
  [:div {:style {:width "100%"
                 :height "90vh"
                 :display "flex"
                 :flex-direction "column"
                 :justify-content "space-between"}}
   [:h1 "Heading"]
   [:p [:a {:href "#"} "I am a link"]]
   [:div
    [:button.button "Click it"]]])
