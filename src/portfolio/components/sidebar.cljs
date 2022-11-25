(ns portfolio.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.components.triangle :refer [Triangle]]
            [portfolio.icons.caret-double-left :as caret-double-left]))

(d/defcomponent Sidebar [{:keys [width title lists actions slide?]}]
  [:div {:style {:background "#e1e4ec"
                 :width (if slide? 0 width)
                 :flex-shrink "0"
                 :transition "width 0.25s ease"}
         :mounted-style {:width width}
         :leaving-style {:width 0}}
   [:div {:style {:margin "20px 0 0 10px"
                  :width 16
                  :height 16
                  :cursor "pointer"}
          :on-click actions}
    caret-double-left/icon]
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
