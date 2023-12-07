(ns portfolio.ui.components.canvas
  (:require [clojure.string :as str]
            [dumdom.core :as d]
            [portfolio.adapter :as adapter]
            [portfolio.ui.actions :as actions]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.browser :refer [Browser]]
            [portfolio.ui.components.code :refer [Code]]
            [portfolio.ui.components.error :refer [Error]]
            [portfolio.ui.components.hud :as hud]
            [portfolio.ui.components.markdown :refer [Markdown]]
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

(defn report-error [cause e scene]
  (actions/dispatch
   (:report-render-error (:actions scene))
   nil
   {:action/exception e
    :action/cause cause}))

(defn render-scene [el {:keys [scene tools opt]}]
  (let [iframe (get-iframe el)
        canvas (some-> iframe .-contentDocument (.getElementById "canvas"))
        error (.querySelector el ".error-container")]
    (when error
      (.removeChild (.-parentNode error) error)
      (set! (.. iframe -style -display) "block"))
    (doseq [tool tools]
      (try
        (canvas/prepare-canvas tool el opt)
        (catch :default e
          (-> (str "Failed to prepare canvas with " (:id tool))
              (report-error e scene)))))
    (try
      (adapter/render-component (assoc scene :component ((:component-fn scene))) canvas)
      (js/setTimeout
       (fn []
         (doseq [tool tools]
           (try
             (canvas/finalize-canvas tool el opt)
             (catch :default e
               (-> (str "Failed to finalize canvas with " (:id tool))
                   (report-error e scene)))))
         (when-let [win (.-contentWindow iframe)]
           (.postMessage
            win
            (clj->js {:event "scene-rendered"
                      :scene-id (->> [(namespace (:id scene))
                                      (name (:id scene))]
                                     (remove empty?)
                                     (str/join "/"))}) "*")))
       50)
      (catch :default e
        (-> (str "Failed to render " (str "'" (:title scene) "'"))
            (report-error e scene))))))

(defn on-mounted [el f]
  (if (some-> el .-contentDocument (.getElementById "canvas"))
    (f)
    (.addEventListener
     el "load"
     (fn [_]
       (let [doc (->> el .-contentDocument)]
         (set! (.. doc -documentElement -style -height) "auto")
         (when-not (.getElementById doc "canvas")
           (let [el (.createElement doc "div")]
             (set! (.-id el) "canvas")
             (.appendChild (.-body doc) el)))
         (f))))))

(defn init-canvas [el data f]
  (let [iframe (get-iframe el)
        document (get-iframe-document el)
        head (.-head document)
        loaded (atom 0)
        target-n (+ (count (:css-paths data)) (count (:script-paths data)))
        try-complete #(when (= target-n @loaded)
                        (f))]
    (set! (.-title document) "Component scene")
    (.addEventListener
     (.-contentWindow iframe)
     "message"
     (fn [e]
       (js/window.postMessage (.-data e))))
    (try-complete)

    ;; Load CSS files
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

    ;; Load scripts
    (doseq [path (:script-paths data)]
      (let [link (js/document.createElement "script")]
        (set! (.-type link) "text/javascript")
        (set! (.-src link) path)
        (.addEventListener
         link "load"
         (fn [_]
           (swap! loaded inc)
           (try-complete)))
        (.appendChild head link)))

    ;; Set padding properties
    (let [[t r b l] (:viewport/padding (:opt data))]
      (when t (set! (.. document -body -style -paddingTop) (str t "px")))
      (when r (set! (.. document -body -style -paddingBottom) (str r "px")))
      (when b (set! (.. document -documentElement -style -paddingLeft) (str b "px")))
      (when l (set! (.. document -documentElement -style -paddingRight) (str l "px"))))))

(defn get-rendered-data [{:keys [scene opt]}]
  {:rendered (:rendered-data scene)
   :portfolio-options opt})

(defn process-render-queue [el]
  (when (.-renderFromQueue el)
    (on-mounted
     (get-iframe el)
     #(when-let [data (.-renderQueue el)]
        (set! (.-renderQueue el) nil)
        (set! (.-renderedData el) (get-rendered-data data))
        (render-scene el data)))))

(defn novel-render? [el data]
  (not= (.-renderedData el) (get-rendered-data data)))

(defn enqueue-render-data [el data]
  (when (novel-render? el data)
    (set! (.-renderQueue el) data)
    (process-render-queue el)))

(d/defcomponent Canvas
  :keyfn (fn [{:keys [css-paths script-paths]}]
           ;; If either of these change, we need to remount the canvas
           (str/join (sort (concat css-paths script-paths))))
  :on-mount (fn [el data]
              (set! (.-renderQueue el) data)
              (on-mounted
               (get-iframe el)
               (fn []
                 (init-canvas
                  el data
                  (fn []
                    (set! (.-renderFromQueue el) true)
                    (process-render-queue el))))))
  :on-update (fn [el data]
               (enqueue-render-data el data))
  [data]
  [:div {:style {:background (or (:background/background-color (:opt data))
                                 "var(--canvas-bg)")
                 :display "flex"
                 :transition "width 0.25s, height 0.25s"}}
   [:iframe.canvas
    {:src (or (:canvas-path data) "/portfolio/canvas.html")
     :title "Component scene"
     :style {:border "none"
             :flex-grow "1"
             :width (or (when (number? (:viewport/width (:opt data)))
                          (:viewport/width (:opt data)))
                        "100%")
             :height (when (number? (:viewport/height (:opt data)))
                       (:viewport/height (:opt data)))}}]])

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
  [{:keys [title url description code]}]
  [:div {:style {:margin "20px"}}
   [:h2.h3 {:style (when-not (empty? description)
                     {:margin "0 0 10px"})}
    (if url
      [:a {:href url} title]
      title)]
   (when-not (empty? description)
     (Markdown {:markdown description
                :tag :p}))
   (when-not (empty? code)
     [:div {:style {:margin "20px 0"}}
      [:h3.h4 {:style {:margin "10px 0"}}
       "Scene code"]
      (Code {:code code})])])

(defn render-canvas [data]
  [:div.canvas-wrapper
   (->> [(when (not-empty (select-keys data [:title :description :code]))
           (CanvasHeader data))
         (when (:scene data)
           (if (or (not (:component-fn (:scene data)))
                   (:error (:scene data)))
             (Error (:error (:scene data)))
             (Canvas data)))]
        (remove nil?))])

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
         :overflow "hidden"}
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

(defn render-hud [hud]
  (hud/render-hud
   {:action (:action hud)
    :style {:position "absolute"
            :bottom 20
            :left 20
            :right 20}}
   (when-let [error (:error hud)]
     (Error error))))

(d/defcomponent Pane
  :keyfn :id
  [{:keys [toolbar canvases title description background menu-bar browser handle class-name] :as data}]
  [:div.pane
   {:style (into (get-grid-styles data)
                 {:min-height "100%"
                  :display "flex"
                  :flex-direction "column"})}
   (some-> toolbar Toolbar)
   (some-> menu-bar WrappedMenuBar)
   [:div {:style (merge {:overflow "auto"
                         :flex-grow 1
                         :transition "background 0.15s"
                         :background background})
          :class (if (:items browser)
                   :dark
                   class-name)}
    (when (:items browser)
      (Browser browser))
    (when (or title description)
      [:div {:style {:margin "20px"}}
       (when title
         [:h1.h1 title])
       (when description
         (Markdown {:markdown description}))])
    (when (seq canvases)
      (map render-canvas canvases))]
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
   [:div {:style {:flex-grow 1
                  :position "relative"}}
    (render-layout data)]
   (some-> data :hud render-hud)
   (some-> data :panel CanvasPanel)])
