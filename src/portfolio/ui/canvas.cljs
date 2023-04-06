(ns portfolio.ui.canvas
  (:require [markdown.core :as md]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.code :as code]
            [portfolio.ui.components.canvas :refer [CanvasView]]
            [portfolio.ui.layout :as layout]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]
            [portfolio.ui.view :as view]))

(def view-impl
  {`view/render-view #'CanvasView})

(extend-type cljs.core/PersistentArrayMap
  canvas/ICanvasToolValue
  (get-tool-value [tool state canvas-id]
    (get-in state [:panes canvas-id (:id tool) :value])))

(defn get-current-addon [location addons]
  (or (when-let [id (some-> location :query-params :addon keyword)]
        (first (filter (comp #{id} :id) addons)))
      (first addons)))

(defn prepare-panel [state location scene addons]
  (let [current-addon (get-current-addon location addons)
        minimize-path [:canvas/panel :minimized?]
        content (canvas/prepare-panel-content current-addon state scene)
        minimized? (get-in state minimize-path (not content))]
    {:tabs (for [addon addons]
             (cond-> addon
               (= current-addon addon)
               (assoc :selected? true)

               (not= current-addon addon)
               (assoc :url (routes/get-addon-url location addon))))
     :minimized? minimized?
     :button (if minimized?
               {:text "Maximize"
                :actions [[:dissoc-in minimize-path]]
                :direction :up}
               {:text "Minimize"
                :direction :down
                :actions [[:assoc-in minimize-path true]]})
     :content content}))

(defn get-tool-defaults [tools]
  (apply merge (map :default-value tools)))

(defn toolbar-value? [tool]
  (or (satisfies? canvas/ICanvasToolValue tool)
      (ifn? (get (meta tool) `canvas/get-tool-value))))

(defn get-tool-values [state id tools]
  (->> tools
       (filter toolbar-value?)
       (map #(canvas/get-tool-value % state id))
       (apply merge)))

(defn prepare-canvas [options canvas]
  (let [f (-> canvas :scene :component-fn)
        canvas (assoc canvas :opt options)]
    (try
      (cond-> canvas ;; (assoc canvas :id (::layout/pane-id (:opt canvas)))
        (ifn? f) (assoc-in [:scene :component] (f options)))
      (catch :default e
        (assoc-in canvas
         [:scene :error]
         {:message (.-message e)
          :ex-data (code/code-str (ex-data e))
          :stack (.-stack e)
          :title "Failed to render component"})))))

(defn toolbar-button? [tool]
  (or (satisfies? canvas/ICanvasToolbarButtonData tool)
      (ifn? (get (meta tool) `canvas/prepare-toolbar-button))))

(defn prepare-layout-xs [state root-layout source view scenes path opt]
  (if (#{:rows :cols} (:kind opt))
    {:kind (:kind opt)
     :xs (for [[i x] (map vector (range) (:xs opt))]
           (prepare-layout-xs state root-layout source view scenes (conj path i) x))}
    (let [id (::layout/pane-id opt)
          options (merge (get-tool-defaults (:tools view))
                         opt
                         (get-tool-values state id (:tools view)))]
      (when (seq scenes)
        (let [buttons (->> (:tools view)
                           (filter toolbar-button?)
                           (keep #(canvas/prepare-toolbar-button
                                   % state {:pane-id id
                                            :pane-options options
                                            :pane-path path
                                            :layout-path [:layout source]
                                            :layout root-layout
                                            :config-source source})))]
          (cond-> {:kind :pane
                   :canvases (map (partial prepare-canvas options) scenes)}
            (seq buttons)
            (assoc :toolbar {:buttons buttons})))))))

(defn canvas-tool? [tool]
  (or (satisfies? canvas/ICanvasTool tool)
      (and (ifn? (get (meta tool) `canvas/prepare-canvas))
           (ifn? (get (meta tool) `canvas/finalize-canvas)))))

(defn prepare-layout [state location view {:keys [layout source]} scenes]
  (let [scenes (for [scene (sort-by scene/sort-key scenes)]
                 (let [tools (filter canvas-tool? (:tools view))]
                   (cond->
                       {:scene (scene/prep-scene-fn state scene)}
                     (seq (:css-paths state))
                     (assoc :css-paths (:css-paths state))

                     (:canvas-path state)
                     (assoc :canvas-path (:canvas-path state))

                     (seq tools)
                     (assoc :tools tools)

                     (:docs scene)
                     (assoc :title (:title scene)
                            :description (md/md->html (:docs scene)))

                     (:gallery? layout)
                     (assoc :title (:title scene)
                            :url (routes/get-scene-url location scene)))))]
    (-> (prepare-layout-xs state layout source view scenes [] layout)
        (assoc :id (if (:gallery? layout)
                     (routes/get-id location)
                     :single-scene)))))

(defn prepare-canvas-view [view state location]
  (let [layout (layout/get-current-layout state)
        {:keys [scenes kind target]} (:current-selection state)]
    (with-meta
      (cond-> (if-let [problems (:problems view)]
                {:problems problems}
                (assoc (prepare-layout state location view layout scenes)
                       :panel (when (and (= 1 (count scenes)) (seq (:addons view)))
                                (prepare-panel state location (first scenes) (:addons view)))))
        (and (:docs target) (= :collection kind))
        (assoc
         :title (:title target)
         :description (md/md->html (:docs target))))
      view-impl)))

(def data-impl
  {`view/prepare-data #'prepare-canvas-view})

(defn describe-missing-tool-id [tool]
  {:title "Badly configured canvas tool"
   :text [:span "Canvas tool extensions must have an " [:code ":id"] " or they won't work correctly. Please inspect this tool:"]
   :code (code/code-str tool)})

(defn create-canvas [{:keys [tools addons layout]}]
  (-> {:id ::canvas
       :title "Canvas"
       :tools (filter :id tools)
       :addons addons
       :layout (or layout {})
       :problems (->> (remove :id tools)
                      (map describe-missing-tool-id)
                      seq)}
      (with-meta data-impl)))
