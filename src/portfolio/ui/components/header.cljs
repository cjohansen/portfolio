(ns portfolio.ui.components.header
  (:require [dumdom.core :as d]
            [phosphor.icons :as icons]
            [portfolio.ui.components.browser :refer [Browser]]
            [portfolio.ui.components.menu-bar :refer [MenuBar]]))

(d/defcomponent Header [{:keys [menu-bar left-action menu]}]
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
    (when (:icon left-action)
      [:span {:on-click (:actions left-action)}
       (icons/render (:icon left-action) {:size 16})])
    (MenuBar menu-bar)]
   (when menu
     (Browser menu))])
