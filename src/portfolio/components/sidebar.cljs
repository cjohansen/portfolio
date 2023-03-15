(ns portfolio.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.icons.caret-double-left :as caret-double-left]
            [portfolio.icons.caret-down :as caret-down]
            [portfolio.icons.bookmark :as bookmark]
            [portfolio.icons.caret-right :as caret-right]
            [portfolio.icons.cube :as cube]
            [portfolio.icons.folder :as folder]))

(d/defcomponent Sidebar [{:keys [width title lists actions slide?]}]
  [:div {:style {:width (if slide? 0 width)
                 :flex-shrink "0"
                 :overflow-y "scroll"
                 :transition "width 0.25s ease"}
         :mounted-style {:width width}
         :leaving-style {:width 0}}
   [:div {:style {:margin "16px 0 16px 8px"
                  :width 16
                  :height 16
                  :cursor "pointer"}
          :on-click actions}
    caret-double-left/icon]
   (when title [:h1.h1 {:style {:margin "20px 10px"}} title])
   (for [list lists]
     [:div {:style {:margin-bottom 20}}
      (when (:title list)
        [:h2.h4 {:style {:background "var(--tuna)"
                         :border-top "1px solid var(--shark-dark)"
                         :border-bottom "1px solid var(--shark-dark)"
                         :padding "16px 8px"
                         :margin "8px 0"
                         :display "flex"
                         :align-items "center"
                         :gap 8}}
         [:div {:style {:width 24
                        :height 24
                        :color "var(--cadet-blue)"}} folder/icon]
         (:title list)])
      [:ul
       (for [{:keys [title expanded? items expand-actions actions]} (:items list)]
         [:li
          (when title
            [:div {:on-click expand-actions
                   :style {:display "flex"
                           :align-items "center"
                           :padding-left 8}}
             [:div {:style {:width 16
                            :height 16
                            :margin-right 8}}
              (if expanded?
                caret-down/icon
                caret-right/icon)]
             [:div {:style {:width 16
                            :height 16
                            :margin-right 8
                            :color "var(--azure-radiance)"}}
              cube/icon]
             [:span {:on-click actions
                     :style {:cursor (when actions "pointer")
                             :display "inline-block"
                             :padding "8px 0"}}
              title]])
          [:ul
           (for [{:keys [title selected? url]} items]
             [:li.text-s {:style {:background (when selected? "var(--mariner)")
                                  :font-weight (when selected? 600)
                                  :display "flex"
                                  :align-items "center"
                                  :margin "4px 4px"
                                  :border-radius 4
                                  :padding "5px 38px"}}
              [:div {:style {:width 16
                             :height 16
                             :margin-right 8
                             :color (when-not selected? "var(--silver-tree)")}}
               bookmark/icon]
              (if selected?
                [:strong title]
                [:a {:style {:display "block"
                             :color "#fff"}
                     :href url} title])])]])]])])
