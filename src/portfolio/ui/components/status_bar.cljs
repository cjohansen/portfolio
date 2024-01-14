(ns portfolio.ui.components.status-bar
  (:require [dumdom.core :as d]))

(d/defcomponent StatusBar [{:keys [label]}]
  [:div
   [:h2 label "!"]])
