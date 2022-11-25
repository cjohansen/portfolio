(ns portfolio.components.header
  (:require [dumdom.core :as d]
            [portfolio.icons.hamburger :as hamburger]))

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
   [:div {:style {:margin-right 10
                  :margin-top 10
                  :width 24
                  :height 24
                  :cursor "pointer"}
          :on-click actions}
    hamburger/icon]
   [:h1.h2 {:style {:margin-top 10}}
    title]])
