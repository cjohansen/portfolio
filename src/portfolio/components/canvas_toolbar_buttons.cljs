(ns portfolio.components.canvas-toolbar-buttons
  (:require [dumdom.core :as d]
            [portfolio.components.popup-menu :refer [PopupMenu]]
            [portfolio.views.canvas.protocols :as protocols]))

(d/defcomponent MenuButton [{:keys [text icon title menu active? actions]}]
  [:span {:style {:display "block"
                  :position "relative"}}
   [:button.button.boldable
    {:title (or title text)
     :style {:color (if menu "#1ea7fd" "#000")
             :display "block"
             :font-weight (when active? "bold")
             :padding "10px 0"
             :width (when icon 20)}
     :on-click actions}
    (when icon
      [:span {:style {:display "block"}}
       icon])
    text]
   (some-> menu PopupMenu)])

(def Button MenuButton)

(d/defcomponent ButtonGroup [{:keys [buttons]}]
  [:div {:style {:display "flex"
                 :flex-direction "row"
                 :gap 10}}
   (for [button buttons]
     (protocols/render-toolbar-button button))])
