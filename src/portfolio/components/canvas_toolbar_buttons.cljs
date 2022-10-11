(ns portfolio.components.canvas-toolbar-buttons
  (:require [dumdom.core :as d]
            [portfolio.components.popup-menu :refer [PopupMenu]]))

(d/defcomponent MenuButton [{:keys [text menu active? actions]}]
  [:span {:style {:margin-left 20
                  :display "inline-block"
                  :position "relative"}}
   [:button.button.boldable
    {:title text
     :style {:color (if menu "#1ea7fd" "#000")
             :font-weight (when active? "bold")
             :padding "10px 0"}
     :on-click actions}
    text]
   (some-> menu PopupMenu)])

(def Button MenuButton)
