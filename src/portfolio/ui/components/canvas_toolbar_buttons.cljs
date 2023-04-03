(ns portfolio.ui.components.canvas-toolbar-buttons
  (:require [dumdom.core :as d]
            [portfolio.icons :as icons]
            [portfolio.ui.canvas.protocols :as protocols]
            [portfolio.ui.components.popup-menu :refer [PopupMenu]]))

(d/defcomponent MenuButton [{:keys [text icon title align menu active? actions]}]
  [:span {:style (cond-> {:display "flex"
                          :position "relative"}
                   (= align :right) (assoc :flex "1" :justify-content "flex-end"))}
   [:button.button.boldable
    {:title (or title text)
     :style {:color (if menu "#1ea7fd" "var(--fg)")
             :display "block"
             :font-weight "bold"
             :font-size 14
             :padding "10px 0"
             :width (when icon 20)}
     :on-click actions}
    (when icon
      (icons/render-icon icon {:size 20}))
    text]
   (some-> menu PopupMenu)])

(def Button MenuButton)

(d/defcomponent ButtonGroup [{:keys [buttons]}]
  [:div {:style {:display "flex"
                 :flex-direction "row"
                 :gap 10}}
   (for [button buttons]
     (protocols/render-toolbar-button button))])
