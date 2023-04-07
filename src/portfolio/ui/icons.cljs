(ns portfolio.ui.icons
  (:require [portfolio.ui.icons.arrow-counter-clockwise :as arrow-counter-clockwise]
            [portfolio.ui.icons.bookmark :as bookmark]
            [portfolio.ui.icons.browsers :as browsers]
            [portfolio.ui.icons.caret-double-left :as caret-double-left]
            [portfolio.ui.icons.caret-double-right :as caret-double-right]
            [portfolio.ui.icons.caret-down :as caret-down]
            [portfolio.ui.icons.caret-right :as caret-right]
            [portfolio.ui.icons.caret-up :as caret-up]
            [portfolio.ui.icons.columns :as columns]
            [portfolio.ui.icons.cross :as cross]
            [portfolio.ui.icons.cube :as cube]
            [portfolio.ui.icons.device-mobile :as device-mobile]
            [portfolio.ui.icons.devices :as devices]
            [portfolio.ui.icons.file-doc :as file-doc]
            [portfolio.ui.icons.folder :as folder]
            [portfolio.ui.icons.folder-open :as folder-open]
            [portfolio.ui.icons.grid-four :as grid-four]
            [portfolio.ui.icons.hamburger :as hamburger]
            [portfolio.ui.icons.list-plus :as list-plus]
            [portfolio.ui.icons.magnifying-glass-minus :as magnifying-glass-minus]
            [portfolio.ui.icons.magnifying-glass-plus :as magnifying-glass-plus]
            [portfolio.ui.icons.package-icon :as package]
            [portfolio.ui.icons.palette :as palette]
            [portfolio.ui.icons.rows :as rows]))

(defn render-icon [icon & [{:keys [size color style on-click]}]]
  [:span {:on-click on-click
          :style
          (cond-> {:display "inline-block"}
            size (assoc :height size)
            size (assoc :width size)
            color (assoc :color color)
            on-click (assoc :cursor "pointer")
            style (into style))}
   (case icon
     ::arrow-counter-clockwise arrow-counter-clockwise/icon
     ::bookmark bookmark/icon
     ::browsers browsers/icon
     ::caret-double-left caret-double-left/icon
     ::caret-double-right caret-double-right/icon
     ::caret-down caret-down/icon
     ::caret-right caret-right/icon
     ::caret-up caret-up/icon
     ::columns columns/icon
     ::cross cross/icon
     ::cube cube/icon
     ::device-mobile device-mobile/icon
     ::devices devices/icon
     ::file-doc file-doc/icon
     ::folder folder/icon
     ::folder-open folder-open/icon
     ::hamburger hamburger/icon
     ::grid-four grid-four/icon
     ::list-plus list-plus/icon
     ::magnifying-glass-plus magnifying-glass-plus/icon
     ::magnifying-glass-minus magnifying-glass-minus/icon
     ::package package/icon
     ::palette palette/icon
     ::rows rows/icon)])
