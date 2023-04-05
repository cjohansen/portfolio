(ns portfolio.ui.components.sidebar
  (:require [dumdom.core :as d]
            [portfolio.ui.icons :as icons]))

(declare render-menu)

(defn render-items
  ([items] (render-items nil items))
  ([attrs items]
   (when (seq items)
     [:ul attrs
      (for [item items]
        [:li (render-menu item)])])))

(defn get-context-offset [context kind]
  (->> (concat context [kind])
       (partition 2 1)
       (map (fn [pair]
              (case pair
                [:folder :folder] 24
                [:folder :package] 0
                [:package :folder] 24
                [:package :package] 24
                [:folder :unit] 32
                [:package :unit] 48)))
       (reduce + 0)))

(d/defcomponent Folder [{:keys [title illustration actions items context]}]
  (let [left-padding (+ 8 (get-context-offset context :folder))]
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

(d/defcomponent Package [{:keys [title illustration actions toggle items context selected?]}]
  [:div
   [:div {:style {:background (when selected? "var(--mariner)")
                  :display "flex"
                  :align-items "center"
                  :border-radius 4
                  :font-weight (when selected? "bold")
                  :padding-left (+ (if selected? 4 8)
                                   (get-context-offset context :package))
                  :margin (when selected?
                            "0 4px")}}
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
        :color (if selected?
                 "#ffffff"
                 (:color illustration))
        :style {:margin-right 8}}))
    [:span {:on-click actions
            :style {:cursor (when actions "pointer")
                    :display "inline-block"
                    :padding "8px 0"}}
     title]]
   (render-items items)])

(d/defcomponent Unit [{:keys [title url selected? illustration context]}]
  (let [left-padding (get-context-offset context :unit)]
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
