(ns portfolio.components.canvas
  (:require [dumdom.core :as d]
            [portfolio.components.tab-bar :refer [TabBar]]
            [portfolio.components.triangle :refer [TriangleButton]]
            [portfolio.protocols :as portfolio]))

(defn get-iframe [canvas-el]
  (some-> canvas-el .-firstChild))

(defn get-iframe-body [canvas-el]
  (some-> canvas-el get-iframe .-contentWindow .-document .-body))

(defn render-scene [el {:keys [scene tools opt]}]
  (doseq [tool tools]
    (portfolio/prepare-layer tool el opt))
  (let [canvas (some-> el .-firstChild .-contentDocument (.getElementById "canvas"))]
    (portfolio/render-component (:component scene) canvas))
  (js/requestAnimationFrame
   (fn [_]
     (doseq [tool tools]
       (portfolio/finalize-layer tool el opt)))))

(defn on-mounted [el f]
  (if (some-> el .-contentDocument (.getElementById "canvas"))
    (f)
    (.addEventListener
     el "load"
     (fn [_]
       (let [doc (->> el .-contentDocument)]
         (when-not (.getElementById doc "canvas")
           (let [el (doc.createElement "div")]
             (set! (.-id el) "canvas")
             (.appendChild (.-body doc) el)))
         (f))))))

(d/defcomponent Canvas
  :on-mount (fn [el data]
              (on-mounted (.-firstChild el) #(render-scene el data)))
  :on-update (fn [el data]
               (on-mounted (.-firstChild el) #(render-scene el data)))
  [data]
  [:div {:style {:background "#f8f8f8"
                 :transition "width 0.25s, height 0.25s"}}
   [:iframe
    {:src "/portfolio/canvas.html"
     :style {:border "none"
             :padding 20
             :width "100%"
             :height "100%"}}]])

(d/defcomponent PopupMenu [{:keys [options]}]
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
                :text-align "left"
                :padding "10px 20px"}
        :on-click actions}
       title])]])

(d/defcomponent ToolbarButton [{:keys [title menu active? actions]}]
  [:span {:style {:margin-left 20
                  :display "inline-block"
                  :position "relative"}}
   [:button.button.boldable
    {:title title
     :style {:color (if menu "#1ea7fd" "#000")
             :font-weight (when active? "bold")
             :padding "10px 0"}
     :on-click actions}
    title]
   (some-> menu PopupMenu)])

(d/defcomponent Toolbar [{:keys [tools]}]
  [:nav {:style {:background "#fff"
                 :border-bottom "1px solid #e5e5e5"}}
   (map ToolbarButton tools)])

(d/defcomponent CanvasPanel [data]
  [:div {:style {:border-top "1px solid #ccc"
                 :background "#ffffff"
                 :height (if (:minimized? data) "40px" "30%")
                 :transition "height 0.25s"
                 :position "relative"}}
   (when-let [button (:button data)]
     [:div {:style {:position "absolute"
                    :right 20
                    :top 10}}
      (TriangleButton button)])
   (TabBar data)
   (some-> data :content portfolio/render-view)])

(d/defcomponent CanvasHeader [{:keys [title url description]}]
  [:div {:style {:margin 20}}
   [:h2.h3 {:style {:margin "0 0 10px"}}
    [:a {:href url} title]]
   (when-not (empty? description)
     [:p description])])

(defn render-canvas [data]
  (->> [(when (:title data)
          (CanvasHeader data))
        (when (:scene data)
          (Canvas data))
        (when (= :separator (:kind data))
          [:div {:style {:height 20}}])]
       (remove nil?)))

(d/defcomponent CanvasView [data]
  [:div {:style {:background "#eee"
                 :flex-grow 1
                 :display "flex"
                 :flex-direction "column"
                 :overflow "hidden"}}
   (->> (for [row (:rows data)]
          [:div {:style {:display "flex"
                         :flex-direction "row"
                         :flex-grow 1
                         :justify-content "space-evenly"
                         :overflow "hidden"}}
           (->> (for [{:keys [toolbar canvases]} row]
                  [:div {:style {:flex-grow 1
                                 :display "flex"
                                 :flex-direction "column"
                                 :overflow "hidden"}}
                   (some-> toolbar Toolbar)
                   [:div {:style {:overflow "scroll"
                                  :flex-grow "1"}}
                    (->> canvases
                         (interpose {:kind :separator})
                         (mapcat render-canvas))]])
                (interpose [:div {:style {:border-left "5px solid #ddd"}}]))])
        (interpose [:div {:style {:border-top "5px solid #ddd"}}]))
   (some-> data :panel CanvasPanel)])
