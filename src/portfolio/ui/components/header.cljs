(ns portfolio.ui.components.header
  (:require [dumdom.core :as d]
            [portfolio.ui.components.browser :as browser]
            [portfolio.ui.components.elastic-container :as ec]
            [portfolio.ui.components.menu-bar :refer [MenuBar]]
            [portfolio.ui.icons :as icons]))

(d/defcomponent HeaderMenu
  :will-enter (ec/enter)
  :will-leave (ec/leave)
  [{:keys [items]}]
  (browser/render-items items))

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
      (icons/render-icon
       (:icon left-action)
       {:size 16
        :on-click (:actions left-action)}))
    (MenuBar menu-bar)]
   (when menu
     (HeaderMenu menu))])
