(ns portfolio.ui.components.canvas
  (:require [cljs.pprint :as pprint]
            [dumdom.core :as d]
            [portfolio.adapter :as adapter]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.code :refer [Code]]
            [portfolio.ui.components.markdown :refer [Markdown]]
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
                 :transition "width 0.25s, height 0.25s"}}
   [:iframe.canvas
    {:src (or (:canvas-path data) "/portfolio/canvas.html")
     :style {:border "none"
             :width (or (when (number? (:viewport/width (:opt data)))
                          (:viewport/width (:opt data)))
                        "100%")
             :height (or (when (number? (:viewport/height (:opt data)))
                           (:viewport/height (:opt data)))
                         "100%")}}]])

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

(defn render-layout [data]
  (if (#{:rows :cols} (:kind data))
    [:div {:style {:flex-grow 1
                   :display "flex"
                   :flex-direction (direction (:kind data))}}
     (->> (map render-layout (:xs data))
          (interpose [:div {:style {(if (= :rows (:kind data))
                                      :border-top
                                      :border-left)
                                    "3px solid var(--hard-separator)"}}]))]
    (let [{:keys [toolbar canvases title description]} data]
      [:div {:style {:flex-grow 1
                     :display "flex"
                     :flex-direction "column"}}
       (some-> toolbar Toolbar)
       [:div {:style {:overflow "scroll"
                      :flex-grow "1"}}
        (when (or title description)
          [:div {:style {:margin 20}}
           (when title
             [:h1.h1 title])
           (when description
             (Markdown {:markdown description}))])
        (->> canvases
             (interpose {:kind :separator})
             (mapcat render-canvas))]])))

(d/defcomponent CanvasView
  :keyfn :id
  [data]
  [:div {:style {:flex-grow 1
                 :display "flex"
                 :flex-direction "column"}}
   (when-let [problems (:problems data)]
     [:div {:style {:overflow "scroll"}}
      (map Problem problems)])
   (render-layout data)
   (some-> data :panel CanvasPanel)])
