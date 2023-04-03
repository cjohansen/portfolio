(ns portfolio.ui.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.icons :as icons]))

(declare render-menu)

(defn render-items
  ([items] (render-items nil items))
  ([attrs items]
   (when (seq items)
     [:ul attrs
      (for [item items]
        [:li (render-menu item)])])))

(defn get-context-offset [context]
  (* (count (filter #{:folder :package} context)) 24))

(d/defcomponent Folder [{:keys [title illustration actions items context]}]
  (let [left-padding (+ 8 (get-context-offset context))]
    [:div {:style {}}
     [:h2.h4
      {:on-click actions
       :style {:background "var(--tuna)"
               :border-top "1px solid var(--shark-dark)"
               :border-bottom "1px solid var(--shark-dark)"
               :padding (str "16px 8px 16px " left-padding "px")
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
     (render-items {:style {:margin "8px 0 24px"}} items)]))

(d/defcomponent Package [{:keys [title illustration actions toggle items context]}]
  [:div
   [:div {:style {:display "flex"
                  :align-items "center"
                  :padding-left (+ 8 (get-context-offset context))}}
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

(d/defcomponent Unit [{:keys [title url selected? illustration context]}]
  (let [left-padding (+ (get-context-offset context)
                        (if (= :package (last context)) 24 0))]
    [:li.text-s {:style {:background (when selected? "var(--mariner)")
                         :font-weight (when selected? 600)
                         :display "flex"
                         :align-items "center"
                         :margin "4px 4px"
                         :border-radius 4
                         :padding (str "8px 0 8px " left-padding "px")}}
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
            :href url} title])]))

(defn render-menu [props]
  (case (:kind props)
    :folder (Folder props)
    :package (Package props)
    (Unit props)))

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
