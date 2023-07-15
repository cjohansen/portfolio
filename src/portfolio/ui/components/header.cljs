(ns portfolio.ui.components.header
  (:require [dumdom.core :as d]
            [phosphor.icons :as icons]
            [portfolio.ui.components.browser :refer [Browser]]
            [portfolio.ui.components.menu-bar :refer [MenuBar]]))

(d/defcomponent Header [{:keys [menu-bar buttons menu]}]
  [:div
   [:div {:style {:display "flex"
                  :gap 20
                  :flex-shrink "0"
                  :transition "height 0.25s ease"
                  :height 0
                  :overflow "hidden"
                  :align-items "center"
                  :padding 20
                  :border-bottom "1px solid var(--header-border)"}
          :mounted-style {:height 56}
          :leaving-style {:height 0}}
    (for [{:keys [text actions icon]} (remove nil? buttons)]
      [:span {:title text
              :on-click actions}
       (icons/render icon {:size 16})])
    (MenuBar menu-bar)]
   (when menu
     (Browser menu))])
