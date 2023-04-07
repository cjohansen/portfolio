(ns portfolio.ui.components.canvas-toolbar-buttons
  (:require [dumdom.core :as d]
            [portfolio.ui.icons :as icons]
            [portfolio.ui.canvas.protocols :as protocols]
            [portfolio.ui.components.popup-menu :refer [PopupMenu]]))

(d/defcomponent MenuButton [{:keys [text icon title menu active? actions selected?]}]
  (let [selected? (or menu selected?)]
    [:span.canvas-menu-button
     {:style {:display "flex"
              :position "relative"}}
     [:button.button.boldable
      {:title (or title text)
       :style {:color (if selected? "var(--highlight-color)" "var(--fg)")
               :display "block"
               :font-weight "bold"
               :font-size 14
               :padding (if icon 6 "6px 12px")
               :width (when icon 32)
               :height 32
               :background (if selected?
                             "var(--toolbar-button-active)"
                             "var(--toolbar-button)")
               }
       :on-click actions}
      (when icon
        (icons/render-icon icon {:size 20}))
      text]
     (some-> menu PopupMenu)]))

(def Button MenuButton)

(d/defcomponent ButtonGroup [{:keys [buttons]}]
  [:div.canvas-button-group
   {:style {:display "flex"
            :flex-direction "row"
            :gap 1}}
   (for [button buttons]
     (protocols/render-toolbar-button button))])
