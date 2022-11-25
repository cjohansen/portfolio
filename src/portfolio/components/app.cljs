(ns portfolio.components.app
  (:require [dumdom.core :as d]
            [portfolio.components.header :refer [Header]]
            [portfolio.components.sidebar :refer [Sidebar]]
            [portfolio.components.tab-bar :refer [TabBar]]
            [portfolio.view :as view]))

(d/defcomponent App [data]
  [:div {:style {:display "flex"
                 :position "absolute"
                 :left 0
                 :top 0
                 :right 0
                 :bottom 0}}
   (some-> data :sidebar Sidebar)
   [:div {:style {:background "#e1e4ec"
                  :display "flex"
                  :flex-direction "column"
                  :flex-grow 1}}
    (some-> data :header Header)
    [:div {:style {:box-shadow "rgba(0, 0, 0, 0.1) 0px 1px 5px 0px"
                   :border-radius 4
                   :flex-grow 1
                   :display "flex"
                   :flex-direction "column"
                   :margin (if (:sidebar data)
                             "10px 10px 10px 0"
                             10)}}
     (TabBar (:tab-bar data))
     (view/render-view (:view data))]]])
