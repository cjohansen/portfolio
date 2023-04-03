(ns portfolio.views.canvas.zoom
  (:require [portfolio.components.canvas :as canvas]
            [portfolio.components.canvas-toolbar-buttons :refer [Button ButtonGroup]]
            [portfolio.views.canvas.addons :as addons]
            [portfolio.views.canvas.protocols :as protocols]))

(defn reset-canvas-zoom [_ el opt]
  (when-not (contains? opt :zoom/level)
    (let [body (canvas/get-iframe-body el)]
      (set! (.. body -style -transform) "")
      (set! (.. body -style -width) "100%")
      (set! (.. body -style -height) "100%"))))

(defn zoom [el opt]
  (let [body (canvas/get-iframe-body el)
        lvl (:zoom/level opt)]
    (cond
      lvl
      (let [size (str (/ 100 lvl) "%")]
        (set! (.. body -zoomed) true)
        (set! (.. body -style -transform) (str "scale(" lvl ")"))
        (set! (.. body -style -transformOrigin) "left top")
        (set! (.. body -style -width) size)
        (set! (.. body -style -height) size))

      (.. body -zoomed)
      (reset-canvas-zoom nil el opt))))

(defn prepare-zoom-button [tool _state {:keys [pane-options] :as pane}]
  (let [level (or (:zoom/level pane-options) 1)
        increment (or (:zoom-increment tool) 0.25)]
    (with-meta
      {:title (:title tool)
       :icon (:icon tool)
       :active? (if (< 0 increment)
                  (< 1 level)
                  (< level 1))
       :actions [[:assoc-in
                  (addons/get-options-path pane tool)
                  {:zoom/level (+ increment level)}]]}
      {`protocols/render-toolbar-button #'Button})))

(def impl
  {`protocols/prepare-canvas (fn [_ el opt] (zoom el opt))
   `protocols/finalize-canvas (fn [_ _ _])
   `protocols/prepare-toolbar-button #'prepare-zoom-button})

(defn create-zoom-in-tool [config]
  (with-meta
    {:id :canvas/zoom-in
     :group-id :canvas/zoom
     :title "Zoom in"
     :icon :ui.icons/magnifying-glass-plus
     :zoom-increment (or (:zoom-increment config) 0.25)}
    impl))

(defn create-zoom-out-tool [config]
  (with-meta
    {:id :canvas/zoom-out
     :group-id :canvas/zoom
     :title "Zoom out"
     :icon :ui.icons/magnifying-glass-minus
     :zoom-increment (or (:zoom-increment config) -0.25)}
    impl))

(defn create-reset-zoom-tool [config]
  (addons/create-action-button
   {:id :canvas/zoom-reset
    :group-id :canvas/zoom
    :title "Reset zoom"
    :icon :ui.icons/arrow-counter-clockwise
    :prepare-canvas #'reset-canvas-zoom
    :get-actions (fn [tool _ pane]
                   [[:dissoc-in (addons/get-options-path pane tool)]])
    :show? (fn [_ _ {:keys [pane-options]}]
             (and (:zoom/level pane-options)
                  (not= 1 (:zoom/level pane-options))))}))

(defn prepare-button-group [tool state opt]
  (with-meta
    {:buttons (->> (:buttons tool)
                   (keep #(protocols/prepare-toolbar-button % state opt)))}
    {`protocols/render-toolbar-button #'ButtonGroup}))

(defn create-zoom-tool [config]
  (with-meta
    {:id :canvas/zoom
     :buttons [(create-zoom-in-tool config)
               (create-zoom-out-tool config)
               (create-reset-zoom-tool config)]}
    {`protocols/prepare-canvas (fn [_ el opt] (zoom el opt))
     `protocols/finalize-canvas (fn [_ _ _])
     `protocols/prepare-toolbar-button #'prepare-button-group}))
