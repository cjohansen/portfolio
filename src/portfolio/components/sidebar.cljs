(ns portfolio.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.components.triangle :refer [Triangle]]))

(d/defcomponent Sidebar [{:keys [width title lists]}]
  [:div {:style {:width width}}
   [:h1.h1 {:style {:margin "20px 10px"}} title]
   (for [list lists]
     [:div {:style {:margin-bottom 20}}
      (when (:title list)
        [:h2.h4 {:style {:margin "0 10px 10px"}}
         (:title list)])
      [:ul
       (for [{:keys [title selected? expanded? items expand-actions actions]} (:items list)]
         [:li
          (when title
            [:div {:style {:background (when selected? "#1ea7fd26")
                           :padding-left 4}}
             (Triangle
              {:color "rgba(153, 153, 153, 0.6)"
               :direction (if expanded? :down :right)
               :actions expand-actions})
             [:span {:on-click actions
                     :style {:cursor (when actions "pointer")
                             :display "inline-block"
                             :padding "5px 0"}}
              title]])
          [:ul
           (for [{:keys [title selected? url]} items]
             [:li.text-s {:style {:background (when selected? "#1ea7fd")
                                  :padding "5px 20px"}}
              (if selected?
                [:strong title]
                [:a {:style {:display "block"}
                     :href url} title])])]])]])])
