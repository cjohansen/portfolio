(ns portfolio.ui.components.hud
  (:require [phosphor.icons :as icons]))

(defn render-hud [{:keys [action style]} & children]
  (into [:div.hud.light
         {:leaving-style {:opacity 0}
          :mounted-style {:opacity 1}
          :style (merge
                  {:position "relative"
                   :min-height 60
                   :border "1px solid var(--soft-separator)"
                   :border-radius 8
                   :box-shadow "rgb(170, 170, 170) 0 0 1px"
                   :opacity 0
                   :transition "opacity 0.25s"}
                  style)}
         (when action
           [:div {:on-click (:actions action)
                  :style {:position "absolute"
                          :top 20
                          :right 20}}
            [:button.clickable {}
             (icons/render (:icon action) {:size 16
                                           :color "var(--fg)"})]])]
        children))
