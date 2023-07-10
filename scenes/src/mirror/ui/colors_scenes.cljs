(ns mirror.ui.colors-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]))

(portfolio/configure-scenes
 {:title "Colors"})

(defn render-colors [color-list]
  (->> color-list
       (map (fn [color-name]
              [:div {:style {:padding 20
                             :background (str "var(--" (name color-name) ")")}}
               [:span {:style {:background "#fff"
                               :display "inline-block"
                               :padding "2px 6px"
                               :border-radius 4
                               :color "#000000"
                               :box-shadow "0 1px 3px rgba(0, 0, 0, 0.2)"}}
                (name color-name)]]))))

(defscene colors
  [:div
   (render-colors
    [:white
     :gallery
     :alto
     :cadet-blue
     :tuna
     :shark-dark
     :shark
     :cod-gray
     :woodsmoke
     :aquamarine
     :azure-radiance
     :mariner
     :silver-tree])])
