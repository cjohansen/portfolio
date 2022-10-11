(ns portfolio.components.canvas
  (:require [dumdom.core :as d]
            [portfolio.components.tab-bar :refer [TabBar]]
            [portfolio.components.triangle :refer [TriangleButton]]
            [portfolio.protocols :as portfolio]
            [portfolio.views.canvas.protocols :as protocols]))

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

(d/defcomponent Toolbar [{:keys [tools]}]
  [:nav {:style {:background "#fff"
                 :border-bottom "1px solid #e5e5e5"}}
   (map protocols/render-toolbar-button tools)])

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

(d/defcomponent CanvasHeader
  :keyfn :title
  [{:keys [title url description]}]
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
          [:div {:key "separator"
                 :style {:height 20}}])]
       (remove nil?)))

(d/defcomponent CanvasView
  :keyfn :mode
  [data]
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
