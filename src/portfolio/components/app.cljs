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
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :flex-grow 1}}
    (some-> data :header Header)
    [:div {:style {:flex-grow 1
                   :display "flex"
                   :flex-direction "column"
                   :overflow "hidden"}}
     (when (< 1 (or (some-> data :tab-bar :tabs count) 0))
       (TabBar (:tab-bar data)))
     (view/render-view (:view data))]]])
