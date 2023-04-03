(ns portfolio.ui.components.header
  (:require [dumdom.core :as d]
            [portfolio.ui.icons :as icons]))

(d/defcomponent Header [{:keys [title actions]}]
  [:div {:style {:display "flex"
                 :flex-shrink "0"
                 :padding "0 20px"
                 :transition "height 0.25s ease"
                 :height 0
                 :overflow "hidden"
                 :align-items "center"}
         :mounted-style {:height 35}
         :leaving-style {:height 0}}
   (icons/render-icon
    :ui.icons/hamburger
    {:size 24
     :on-click actions
     :style {:margin-top 10}})
   [:h1.h2 {:style {:margin-top 10
                    :margin-right 10}}
    title]])
