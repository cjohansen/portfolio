(ns portfolio.ui.components.sidebar
  (:require [dumdom.core :as d]
            [phosphor.icons :as icons]
            [portfolio.ui.components.auto-complete :refer [AutoCompleter]]
            [portfolio.ui.components.browser :as browser]))

(d/defcomponent Footer [{:keys [buttons width]}]
  [:div.dark
   {:style {:position "absolute"
            :bottom 0
            :padding 20
            :width width}}
   (for [{:keys [text icon actions]} buttons]
     [:a {:on-click actions
          :title text}
      (icons/render icon {:size 16})])])

(d/defcomponent Sidebar [{:keys [width title items lists actions slide? search footer]}]
  [:div {:style {:width (if slide? 0 width)
                 :flex-shrink "0"
                 :overflow-y "auto"
                 :transition "width 0.25s ease"
                 :padding-bottom 60}
         :mounted-style {:width width}
         :leaving-style {:width 0}}
   (icons/render
    (icons/icon :phosphor.regular/caret-double-left)
    {:size 16
     :on-click actions
     :style {:margin "16px 8px"}})
   (when title [:h1.h1 {:style {:margin "20px 10px"}} title])
   (when search
     [:div {:style {:margin "0 0 20px"}}
      (AutoCompleter search)])
   (browser/render-items items)
   (when footer
     (Footer (assoc footer :width width)))])
