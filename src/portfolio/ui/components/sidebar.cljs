(ns portfolio.ui.components.sidebar
  (:require [dumdom.core :as d]
            [phosphor.icons :as icons]
            [portfolio.ui.components.auto-complete :refer [AutoCompleter]]
            [portfolio.ui.components.browser :as browser]))

(d/defcomponent Sidebar [{:keys [width title items lists buttons slide? search footer]}]
  [:div {:style {:width (if slide? 0 width)
                 :flex-shrink "0"
                 :overflow-y "auto"
                 :transition "width 0.25s ease"
                 :padding-bottom 60}
         :mounted-style {:width width}
         :leaving-style {:width 0}}
   [:div {:style {:display "flex"}}
    (for [{:keys [text actions icon]} (remove nil? buttons)]
      [:div {:title text}
       (icons/render
        icon
        {:size 16
         :on-click actions
         :style {:margin "16px 8px"
                 :cursor "pointer"}})])]
   (when title [:h1.h1 {:style {:margin "20px 10px"
                                :font-size "1.5rem"}} title])
   (when search
     [:div {:style {:margin "0 0 20px"}}
      (AutoCompleter search)])
   (browser/render-items items)])
