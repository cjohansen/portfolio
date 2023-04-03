(ns portfolio.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.icons :as icons]))

(declare render-menu)

(defn render-items [items]
  (when (seq items)
    [:ul
     (for [item items]
       [:li (render-menu item)])]))

(d/defcomponent Folder [{:keys [title illustration actions items]}]
  [:div {:style {:margin-bottom 20}}
   [:h2.h4
    {:on-click actions
     :style {:background "var(--tuna)"
             :border-top "1px solid var(--shark-dark)"
             :border-bottom "1px solid var(--shark-dark)"
             :padding "16px 8px"
             :margin "8px 0"
             :display "flex"
             :align-items "center"
             :cursor (when actions "pointer")
             :gap 8}}
    (when (:icon illustration)
      (icons/render-icon
       (:icon illustration)
       {:color (:color illustration)
        :size 24}))
    title]
   (render-items items)])

(d/defcomponent Togglable [{:keys [title illustration actions toggle items]}]
  [:div
   [:div {:style {:display "flex"
                  :align-items "center"
                  :padding-left 8}}
    (when (:icon toggle)
      (icons/render-icon
       (:icon toggle)
       {:size 16
        :style {:margin-right 8}
        :on-click (:actions toggle)}))
    (when (:icon illustration)
      (icons/render-icon
       (:icon illustration)
       {:size 16
        :color (:color illustration)
        :style {:margin-right 8}}))
    [:span {:on-click actions
            :style {:cursor (when actions "pointer")
                    :display "inline-block"
                    :padding "8px 0"}}
     title]]
   (render-items items)])

(d/defcomponent MenuItem [{:keys [title url selected? illustration]}]
  [:li.text-s {:style {:background (when selected? "var(--mariner)")
                       :font-weight (when selected? 600)
                       :display "flex"
                       :align-items "center"
                       :margin "4px 4px"
                       :border-radius 4
                       :padding "8px 0 8px 48px"}}
   (when (:icon illustration)
     (icons/render-icon
      (:icon illustration)
      {:size 16
       :color (:color illustration)
       :style {:margin-right 8}}))
   (if selected?
     [:strong title]
     [:a {:style {:display "block"
                  :color "#fff"}
          :href url} title])])

(defn render-menu [props]
  (case (:kind props)
    :folder (Folder props)
    :togglable (Togglable props)
    (MenuItem props)))

(d/defcomponent Sidebar [{:keys [width title items lists actions slide?]}]
  [:div {:style {:width (if slide? 0 width)
                 :flex-shrink "0"
                 :overflow-y "scroll"
                 :transition "width 0.25s ease"}
         :mounted-style {:width width}
         :leaving-style {:width 0}}
   (icons/render-icon
    :ui.icons/caret-double-left
    {:size 16
     :on-click actions
     :style {:margin "16px 8px"}})
   (when title [:h1.h1 {:style {:margin "20px 10px"}} title])
   (render-items items)])
