(ns portfolio.icons
  (:require [portfolio.icons.arrow-counter-clockwise :as arrow-counter-clockwise]
            [portfolio.icons.bookmark :as bookmark]
            [portfolio.icons.caret-double-left :as caret-double-left]
            [portfolio.icons.caret-down :as caret-down]
            [portfolio.icons.caret-right :as caret-right]
            [portfolio.icons.columns :as columns]
            [portfolio.icons.cross :as cross]
            [portfolio.icons.cube :as cube]
            [portfolio.icons.folder :as folder]
            [portfolio.icons.folder-open :as folder-open]
            [portfolio.icons.hamburger :as hamburger]
            [portfolio.icons.magnifying-glass-minus :as magnifying-glass-minus]
            [portfolio.icons.magnifying-glass-plus :as magnifying-glass-plus]
            [portfolio.icons.package :as package]
            [portfolio.icons.rows :as rows]))

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
     :ui.icons/arrow-counter-clockwise arrow-counter-clockwise/icon
     :ui.icons/bookmark bookmark/icon
     :ui.icons/caret-double-left caret-double-left/icon
     :ui.icons/caret-down caret-down/icon
     :ui.icons/caret-right caret-right/icon
     :ui.icons/columns columns/icon
     :ui.icons/cross cross/icon
     :ui.icons/cube cube/icon
     :ui.icons/folder folder/icon
     :ui.icons/folder-open folder-open/icon
     :ui.icons/hamburger hamburger/icon
     :ui.icons/magnifying-glass-plus magnifying-glass-plus/icon
     :ui.icons/magnifying-glass-minus magnifying-glass-minus/icon
     :ui.icons/package package/icon
     :ui.icons/rows rows/icon)])
