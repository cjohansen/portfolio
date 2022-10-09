(ns portfolio.components.app
  (:require [dumdom.core :as d]
            [portfolio.components.sidebar :refer [Sidebar]]
            [portfolio.components.tab-bar :refer [TabBar]]
            [portfolio.protocols :as portfolio]))

(d/defcomponent App [data]
  [:div {:style {:display "flex"
                 :position "absolute"
                 :left 0
                 :top 0
                 :right 0
                 :bottom 0}}
   (some-> data :sidebar Sidebar)
   [:div {:style {:background "#f8f8f8"
                  :box-shadow "rgba(0, 0, 0, 0.1) 0px 1px 5px 0px"
                  :border-radius 4
                  :flex-grow 1
                  :display "flex"
                  :flex-direction "column"
                  :margin "10px 10px 10px 0"}}
    (TabBar (:tab-bar data))
    (portfolio/render-view (:view data))]])
