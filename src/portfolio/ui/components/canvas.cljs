(ns portfolio.ui.components.canvas
  (:require [cljs.pprint :as pprint]
            [dumdom.core :as d]
            [portfolio.adapter :as adapter]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.code :refer [Code]]
            [portfolio.ui.components.markdown :refer [Markdown]]
            [portfolio.ui.components.browser :refer [Browser]]
            [portfolio.ui.components.menu-bar :refer [MenuBar]]
            [portfolio.ui.components.tab-bar :refer [TabBar]]
            [portfolio.ui.components.triangle :refer [TriangleButton]]
            [portfolio.ui.view :as view]))

(defn get-iframe [canvas-el]
  (some-> canvas-el (.querySelector ".canvas")))

(defn get-iframe-document [canvas-el]
  (some-> canvas-el get-iframe .-contentWindow .-document))

(defn get-iframe-body [canvas-el]
  (some-> canvas-el get-iframe-document .-body))

(d/defcomponent ComponentError [{:keys [component-params error] :as lol}]
  [:div {:style {:background "#fff"
                 :width "100%"
                 :height "100%"
                 :padding 20}}
   [:h1.h1.error (:title error)]
   [:p.mod (:message error)]
   (when component-params
     [:div.vs-s.mod
      [:h2.h3.mod "Component params"]
      (for [param component-params]
        [:p.mod (Code {:code param})])])
   (when-let [data (:ex-data error)]
     [:div.vs-s.mod
      [:h2.h3.mod "ex-data"]
      [:p.mod (Code {:code data})]])
   [:p [:pre (:stack error)]]])

(defn get-error-container [el]
  (or (when-let [el (.querySelector el "error-container")]
        (set! (.-innerHTML el) "")
        el)
      (let [error (js/document.createElement "div")]
        (set! (.-className error) "error-container")
        (set! (.. error -style -overflow) "scroll")
        (.appendChild el error)
        error)))

(defn render-error [el iframe scene e]
  (let [error (get-error-container el)]
    (js/setTimeout #(set! (.. el -style -height) "auto") 100)
    (set! (.. iframe -style -display) "none")
    (d/render
     (ComponentError
      {:component-params (:component-params scene)
       :error {:title "Failed to mount component"
               :message (.-message e)
               :stack (.-stack e)
               :ex-data (when-let [data (ex-data e)]
                          (with-out-str (pprint/pprint data)))}})
     error)))

(defn render-scene [el {:keys [scene tools opt]}]
  (let [iframe (get-iframe el)
        canvas (some-> iframe .-contentDocument (.getElementById "canvas"))
        error (.querySelector el ".error-container")]
    (when error
      (.removeChild (.-parentNode error) error)
      (set! (.. iframe -style -display) "block"))
    (try
      (doseq [tool tools]
        (canvas/prepare-canvas tool el opt))
      (adapter/render-component scene canvas)
      (catch :default e
        (render-error el iframe scene e)))
    (js/setTimeout
     #(try
        (doseq [tool tools]
          (canvas/finalize-canvas tool el opt))
        (catch :default e
          (render-error el iframe scene e)))
     50)))

(defn on-mounted [el f]
  (if (some-> el .-contentDocument (.getElementById "canvas"))
    (f)
    (.addEventListener
     el "load"
     (fn [_]
       (let [doc (->> el .-contentDocument)]
         (set! (.. doc -documentElement -style -height) "auto")
         (when-not (.getElementById doc "canvas")
           (let [el (doc.createElement "div")]
             (set! (.-id el) "canvas")
             (.appendChild (.-body doc) el)))
         (f))))))

(defn init-canvas [el data f]
  (let [document (get-iframe-document el)
        head (.-head document)
        loaded (atom 0)
        try-complete #(when (= (count (:css-paths data)) @loaded)
                        (f))]
    (try-complete)
    (doseq [path (:css-paths data)]
      (let [link (js/document.createElement "link")]
        (set! (.-rel link) "stylesheet")
        (set! (.-type link) "text/css")
        (set! (.-href link) path)
        (.addEventListener
         link "load"
         (fn [_]
           (swap! loaded inc)
           (try-complete)))
        (.appendChild head link)))
    (let [[t r b l] (:viewport/padding (:opt data))]
      (when t (set! (.. document -body -style -paddingTop) (str t "px")))
      (when r (set! (.. document -body -style -paddingBottom) (str r "px")))
      (when b (set! (.. document -documentElement -style -paddingLeft) (str b "px")))
      (when l (set! (.. document -documentElement -style -paddingRight) (str l "px"))))))

(d/defcomponent Canvas
  :on-mount (fn [el data]
              (on-mounted (get-iframe el)
                          (fn []
                            (init-canvas el data #(render-scene el data)))))
  :on-update (fn [el data]
               (on-mounted (get-iframe el) #(render-scene el data)))
  [data]
  [:div {:style {:background "#fff"
                 :display "flex"
                 :transition "width 0.25s, height 0.25s"}}
   [:iframe.canvas
    {:src (or (:canvas-path data) "/portfolio/canvas.html")
     :style {:border "none"
             :flex-grow "1"
             :width (or (when (number? (:viewport/width (:opt data)))
                          (:viewport/width (:opt data)))
                        "100%")
             :height (or (when (number? (:viewport/height (:opt data)))
                           (:viewport/height (:opt data)))
                         ;;"100%"
                         )}}]])

(d/defcomponent Toolbar [{:keys [buttons]}]
  [:nav {:style {:background "var(--bg)"
                 :color "var(--fg)"
                 :border-bottom "1px solid var(--separator)"
                 :display "flex"
                 :gap 10
                 :justify-content "space-between"
                 :min-height 48
                 :padding-left 20
                 :padding-right 20}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap 10}}
    (for [tool (remove (comp #{:right} :align) buttons)]
      (canvas/render-toolbar-button tool))]
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap 10}}
    (for [tool (filter (comp #{:right} :align) buttons)]
      (canvas/render-toolbar-button tool))]])

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
   (some-> data :content view/render-view)])

(d/defcomponent CanvasHeader
  :keyfn :title
  [{:keys [title url description]}]
  [:div {:style {:margin 20}}
   [:h2.h3 {:style {:margin "0 0 10px"}}
    (if url
      [:a {:href url} title]
      title)]
   (when-not (empty? description)
     (Markdown {:markdown description
                :tag :p}))])

(defn render-canvas [data]
  (->> [(when (not-empty (select-keys data [:title :description]))
          (CanvasHeader data))
        (when (:scene data)
          (if (:component (:scene data))
            (Canvas data)
            (ComponentError (:scene data))))
        (when (= :separator (:kind data))
          [:div {:key "separator"
                 :style {:margin "30px 0 20px"}}])]
       (remove nil?)))

(d/defcomponent Problem [{:keys [title text code]}]
  [:div {:style {:background "#fff"
                 :padding 20}}
   [:h2.h2 title]
   [:p.mod text]
   (Code {:code code})])

(def direction
  {:rows "column"
   :cols "row"})

(d/defcomponent WrappedMenuBar [menu-bar]
  [:div
   {:style {:background "var(--bg)"
            :color "var(--fg)"
            :padding "10px 20px"}}
   (MenuBar (assoc menu-bar :size :small))])

(defn get-grid-styles [data]
  (into {:position "absolute"
         :overflow "scroll"}
        (if (:height data)
          {:left 0
           :right 0
           :top (:offset data)
           :height (:height data)}
          {:top 0
           :bottom 0
           :left (:offset data)
           :width (:width data)})))

(defn- touch-x [e]
  (or (some-> e .-changedTouches (aget 0) .-screenX)
      (.-screenX e)))

(defn- touch-y [e]
  (or (some-> e .-changedTouches (aget 0) .-screenY)
      (.-screenY e)))

(defn get-style-n [style prop]
  (js/parseInt (.getPropertyValue style prop) 10))

(defn get-dim [props el]
  (let [style (js/window.getComputedStyle el)]
    (if (= :horizontal (:kind props))
      {:size (get-style-n style "height")
       :offset (get-style-n style "top")}
      {:size (get-style-n style "width")
       :offset (get-style-n style "left")})))

(defn set-size [props el size]
  (if (= :horizontal (:kind props))
    (set! (.. el -style -height) (str size "px"))
    (set! (.. el -style -width) (str size "px"))))

(defn set-offset [props el offset]
  (if (= :horizontal (:kind props))
    (set! (.. el -style -top) (str offset "px"))
    (set! (.. el -style -left) (str offset "px"))))

(defn get-neighbour [el]
  (.-nextSibling el))

(d/defcomponent Handle
  :on-mount (fn [el props]
              (let [state (atom {})
                    f (if (= :horizontal (:kind props)) touch-y touch-x)
                    container (.-parentNode el)]

                (.addEventListener
                 el "mousedown"
                 (fn [e]
                   (swap! state assoc
                          :dragging? true
                          :start (f e)
                          :dim (get-dim props container)
                          :neighbour-dim (get-dim props (get-neighbour container)))
                   (.preventDefault e)
                   (.add (.-classList el) "dragging")))

                (.addEventListener
                 js/document.body
                 "mousemove"
                 (fn [e]
                   (let [{:keys [dragging? start dim neighbour-dim]} @state]
                     (when dragging?
                       (let [offset (- (f e) start)
                             neighbour (get-neighbour container)]
                         (set-size props container (+ (:size dim) offset))
                         (set-offset props neighbour (+ (:offset neighbour-dim) offset))
                         (set-size props neighbour (- (:size neighbour-dim) offset)))))))

                (.addEventListener
                 js/document.body
                 "mouseup"
                 (fn [_e]
                   (when (:dragging? @state)
                     (swap! state dissoc :dragging?)
                     (.remove (.-classList el) "dragging"))))))
  [{:keys [kind]}]
  [:div.draggable {:style (if (= :horizontal kind)
                            {:border-bottom "3px solid var(--hard-separator)"
                             :padding-top 30
                             :position "absolute"
                             :bottom 0
                             :left 0
                             :right 0}
                            {:position "absolute"
                             :border-right "3px solid var(--hard-separator)"
                             :padding-left 20
                             :right 0
                             :top 0
                             :bottom 0})}])

(d/defcomponent Pane
  :keyfn :id
  [{:keys [toolbar canvases title description menu-bar browser handle] :as data}]
  [:div.pane
   {:style (into (get-grid-styles data)
                 {:display "flex"
                  :flex-direction "column"})}
   (some-> toolbar Toolbar)
   (some-> menu-bar WrappedMenuBar)
   [:div {:style (merge {:flex-grow "1"
                         :overflow "scroll"
                         :display "flex"
                         :flex-direction "column"}
                        (when (:items browser)
                          {:background "var(--bg)"
                           :color "var(--fg)"}))}
    (when (:items browser)
      (Browser browser))
    (when (or title description)
      [:div {:style {:margin 20}}
       (when title
         [:h1.h1 title])
       (when description
         (Markdown {:markdown description}))])
    (when (seq canvases)
      (->> canvases
           (interpose {:kind :separator})
           (mapcat render-canvas)))]
   (some-> handle Handle)])

(defn render-layout [data]
  (if (#{:rows :cols} (:kind data))
    [:div {:style (get-grid-styles data)
           :class (:kind data)}
     (map render-layout (:xs data))
     (some-> (:handle data) Handle)]
    (Pane data)))

(d/defcomponent CanvasView
  :keyfn :id
  [data]
  [:div {:style {:flex-grow 1
                 :display "flex"
                 :flex-direction "column"}}
   (when-let [problems (:problems data)]
     [:div {:style {:overflow "scroll"}}
      (map Problem problems)])
   [:div {:style {:flex-grow 1
                  :position "relative"}}
    (render-layout data)]
   (some-> data :panel CanvasPanel)])
