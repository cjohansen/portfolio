(ns portfolio.ui.components.error
  (:require [portfolio.ui.components.code :refer [Code]]
            [dumdom.core :as d]))

(d/defcomponent Error [{:keys [title message data stack]}]
  [:div {:style {:width "100%"
                 :height "100%"
                 :padding 20}}
   [:h1.h1.error title]
   [:p.mod message]
   (for [{:keys [label data]} (remove nil? data)]
     [:div.vs-s.mod
      [:h2.h3.mod label]
      [:p.mod (Code {:code data})]])
   [:p [:pre stack]]])
