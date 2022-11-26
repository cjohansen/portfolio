(ns portfolio.components.canvas-toolbar-buttons
  (:require [dumdom.core :as d]
            [portfolio.components.popup-menu :refer [PopupMenu]]))

(d/defcomponent MenuButton [{:keys [text icon title menu active? actions]}]
  [:span {:style {:margin-left 20
                  :display "inline-block"
                  :position "relative"}}
   [:button.button.boldable
    {:title (or title text)
     :style {:color (if menu "#1ea7fd" "#000")
             :font-weight (when active? "bold")
             :padding "10px 0"}
     :on-click actions}
    (when icon
      [:span {:style {:width 20
                      :display "inline-block"}}
       icon])
    text]
   (some-> menu PopupMenu)])

(def Button MenuButton)
