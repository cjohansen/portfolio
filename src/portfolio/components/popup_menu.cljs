(ns portfolio.components.popup-menu
  (:require [dumdom.core :as d]))

(d/defcomponent PopupMenu
  :on-render (fn [el _]
               (let [left (.. el getBoundingClientRect -left)
                     width (.. el -firstChild getBoundingClientRect -width)
                     button-width (.. el -parentNode getBoundingClientRect -width)]
                 (when (< (- left (/ width 2)) 0)
                   (set! (.. el -style -left) "0")
                   (set! (.. el -firstChild -style -transform) nil)
                   (set! (.. el -firstChild -firstChild -style -left) (str (/ button-width 2) "px")))))
  [{:keys [options]}]
  ;; First, position absolutely so that "50%" is taken as 50% of the containing
  ;; element
  [:div {:style {:position "absolute"
                 :left "50%"}}
   ;; Then position fixed so element is not clipped by a container's overflow:
   ;; hidden
   [:div {:style
          {:position "fixed"
           :margin-top 6
           :background "#fff"
           :box-shadow "rgba(0, 0, 0, 0.1) 0px 1px 5px 0px"
           :border-radius 4
           ;; Shift element back half its own width so it's centered under the
           ;; containing element that triggered it
           :transform "translateX(-50%)"
           :padding "10px 0"
           :width 200
           :z-index 1}}
    [:div {:style
           {:position "absolute"
            :border-style "solid"
            :transform "translate3d(-50%, 0px, 0px)"
            :left "50%"
            :top -8
            :border-width "0px 8px 8px"
            :border-color "transparent transparent rgba(255, 255, 255, 0.95)"}}]
    (for [{:keys [title selected? actions]} options]
      [:button.button.hoverable
       {:style {:background (when selected? "#f8f8f8")
                :font-weight (when selected? "bold")
                :width "100%"
                :cursor (if actions "pointer" "default")
                :text-align "left"
                :padding "10px 20px"}
        :on-click actions}
       title])]])
