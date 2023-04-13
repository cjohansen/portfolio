(ns portfolio.ui.components.app
  (:require [dumdom.core :as d]
            [portfolio.ui.components.header :refer [Header]]
            [portfolio.ui.components.sidebar :refer [Sidebar]]
            [portfolio.ui.components.tab-bar :refer [TabBar]]
            [portfolio.ui.view :as view]))

(d/defcomponent App [data]
  [:div {:style {:display "flex"
                 :position "absolute"
                 :left 0
                 :top 0
                 :right 0
                 :bottom 0}}
   (some-> data :sidebar Sidebar)
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :flex-grow 1
                  :position "relative"
                  :overflow "scroll"}}
    (some-> data :header Header)
    [:div {:style {:flex-grow 1
                   :display "flex"
                   :flex-direction "column"}}
     (when (< 1 (or (some-> data :tab-bar :tabs count) 0))
       (TabBar (:tab-bar data)))
     (view/render-view (:view data))]]])
