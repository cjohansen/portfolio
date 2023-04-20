(ns portfolio.ui.canvas
  (:require [markdown.core :as md]
            [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.code :as code]
            [portfolio.ui.color :as color]
            [portfolio.ui.components.canvas :refer [CanvasView]]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [ButtonGroup]]
            [portfolio.ui.layout :as layout]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]
            [portfolio.ui.view :as view]))

(def view-impl
  {`view/render-view #'CanvasView})

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
  (apply merge (map addons/get-default-value tools)))

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

(defn dark? [background]
  (when background
    (< (:l (color/rgb->hsl (color/->rgb background))) 40)))

(defn create-button-groups [buttons]
  (loop [buttons buttons
         grouped (->> buttons
                      (filter :button-group)
                      (group-by :button-group)
                      (into {}))
         res []]
    (if-let [{:keys [button-group] :as button} (first buttons)]
      (recur
       (next buttons)
       (dissoc grouped button-group)
       (cond
         (nil? button-group)
         (conj res button)

         (grouped button-group)
         (conj res (with-meta
                     {:buttons (grouped button-group)}
                     {`canvas/render-toolbar-button #'ButtonGroup}))

         :else res))
      res)))

(defn specifically-sized? [{:viewport/keys [width height]}]
  (or (number? width) (number? height)))

(defn prepare-pane [state view ctx]
  (when-let [scenes (seq (:scenes ctx))]
    (let [buttons (->> (:tools view)
                       (filter toolbar-button?)
                       (keep #(canvas/prepare-toolbar-button
                               % state (dissoc ctx :scenes)))
                       create-button-groups)
          background (:background/background-color (:pane-options ctx))]
      (cond-> {:kind :pane
               :id (:pane-id ctx)
               :canvases (map (partial prepare-canvas (:pane-options ctx)) scenes)
               :class-name (if (dark? background)
                             :dark
                             :light)}
        (seq buttons)
        (assoc :toolbar {:buttons buttons})

        (and (not (specifically-sized? (:pane-options ctx)))
             (<= (count scenes) 1))
        (assoc :background background)))))

(defn canvas-tool? [tool]
  (or (satisfies? canvas/ICanvasTool tool)
      (and (ifn? (get (meta tool) `canvas/prepare-canvas))
           (ifn? (get (meta tool) `canvas/finalize-canvas)))))

(defn pane-prepper? [tool]
  (or (satisfies? canvas/ICanvasToolPaneMiddleware tool)
      (ifn? (get (meta tool) `canvas/prepare-pane))))

(defn prepare-scenes [state location view layout scenes]
  (for [scene (sort-by scene/sort-key scenes)]
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

        (:code scene)
        (assoc :code (md/md->html (str "\n```clojure\n" (:code scene) "\n```")))

        (:gallery? layout)
        (assoc :title (:title scene)
               :url (routes/get-scene-url location scene))))))

(defn prepare-layout-pane [state view ctx]
  (let [f (->> (:tools view)
               (filter pane-prepper?)
               (reduce (fn [f tool]
                         (partial canvas/prepare-pane tool f))
                       prepare-pane))]
    (f state view ctx)))

(defn prepare-layout-xs [state location root-layout source view scenes path opt]
  (if (#{:rows :cols} (:kind opt))
    {:kind (:kind opt)
     :xs (let [n (count (:xs opt))]
           (for [[i x] (map vector (range) (:xs opt))]
             (-> (prepare-layout-xs state location root-layout source view scenes (conj path i) x)
                 (assoc (if (= :rows (:kind opt))
                          :height
                          :width)
                        (str "calc(100% / " n ")"))
                 (assoc :offset (str "calc((100% / " n ") * " i ")"))
                 ;; Using calc instead of calculating the value here means the
                 ;; size won't be forcibly set unless the layout is different.
                 ;; This allows us to have transient pane resize that isn't
                 ;; represented in the store. Ideally we'll have the size in the
                 ;; store eventually, but this gives us a nice POC for now.
                 (assoc :handle (when (< i (dec n))
                                  {:kind (if (= :rows (:kind opt))
                                           :horizontal
                                           :vertical)})))))}
    (->> {:pane-id (::layout/pane-id opt)
          :pane-options (merge (get-tool-defaults (:tools view))
                               opt
                               (get-tool-values state (::layout/pane-id opt) (:tools view)))
          :pane-path path
          :layout-path [:layout source]
          :layout root-layout
          :config-source source
          :scenes scenes}
         (prepare-layout-pane state view))))

(defn prepare-layout [state location view {:keys [layout source]} scenes]
  (let [scenes (prepare-scenes state location view layout scenes)]
    (-> (prepare-layout-xs state location layout source view scenes [] layout)
        (assoc :id (if (:gallery? layout)
                     (routes/get-id location)
                     :single-scene)
               :height "100%"))))

(defn prepare-canvas-view [state location view]
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

(defn view-prepper? [tool]
  (or (satisfies? canvas/ICanvasToolMiddleware tool)
      (ifn? (get (meta tool) `canvas/prepare-view))))

(defn prepare-view [view state location]
  (let [f (->> (:tools view)
               (filter view-prepper?)
               (reduce (fn [f tool]
                         (partial canvas/prepare-view tool f state location view))
                       prepare-canvas-view))]
    (f state location view)))

(def data-impl
  {`view/prepare-data #'prepare-view})

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
