(ns portfolio.components.toggle
  (:require [dumdom.core :as d]))

(def w 62)
(def h 25)

(def label-styles
  {:position "absolute"
   :top 0
   :color "#ffffff"
   :fontSize "12px"
   :lineHeight (str h "px")
   :textAlign "center"
   :width (* 0.58 w)
   :transition "opacity 300ms ease-in-out"})

(defn off-label-styles [on?]
  (-> label-styles
      (assoc :right 3)
      (assoc :opacity (if on? 0 1))))

(defn on-label-styles [on?]
  (-> label-styles
      (assoc :left 3)
      (assoc :opacity (if on? 1 0))))

(defn prevent-touch-move [node]
  (.addEventListener node "touchstart" #(.stopPropagation %) false)
  (.addEventListener node "touchmove" #(.stopPropagation %) false)
  (.addEventListener node "touchend" #(.stopPropagation %) false))

(d/defcomponent Toggle
  :on-mount prevent-touch-move
  [{:keys [on? actions off-label on-label icon]}]
  [:div {:style {:width w
                 :height h}}
   [:div {:style {:position "relative"
                  :width w
                  :height h
                  :borderRadius (/ h 2)
                  :cursor "pointer"
                  :backgroundColor (if on? "#1ea7fd" "#999")
                  :transition "background-color 300ms ease-in-out"}
          :on-click actions}
    (when off-label
      [:div {:style (off-label-styles on?)} off-label])
    (when on-label
      [:div {:style (on-label-styles on?)} on-label])
    [:div
     {:style
      (let [s (* 0.8 h)]
        {:position "absolute"
         :height s
         :width s
         :borderRadius (/ s 2)
         :top "2.5px"
         :transform (str "translate(" (if on? (str (- w s 2.5) "px") "2.5px") ",0) translateZ(0)")
         :backgroundColor "#ffffff"
         :transition "transform 300ms ease-in-out"})}]]])
