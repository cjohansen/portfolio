(ns portfolio.views.canvas.zoom
  (:require [portfolio.components.canvas :as canvas]
            [portfolio.components.canvas-toolbar-buttons :refer [Button]]
            [portfolio.icons.arrow-counter-clockwise :as reset]
            [portfolio.icons.magnifying-glass-minus :as minus]
            [portfolio.icons.magnifying-glass-plus :as plus]
            [portfolio.views.canvas.addons :as addons]
            [portfolio.views.canvas.protocols :as protocols]))

(defn zoom [el opt]
  (when-let [lvl (:zoom/level opt)]
    (let [body (canvas/get-iframe-body el)
          size (str (/ 100 lvl) "%")]
      (set! (.. body -style -transform) (str "scale(" lvl ")"))
      (set! (.. body -style -transformOrigin) "left top")
      (set! (.. body -style -width) size)
      (set! (.. body -style -height) size))))

(defn prepare-zoom-button [tool state {:keys [pane-id pane-options]}]
  (let [level (or (:zoom/level pane-options) 1)
        increment (or (:zoom-increment tool) 0.25)]
    (with-meta
      {:title (:title tool)
       :icon (:icon tool)
       :active? (if (< 0 increment)
                  (< 1 level)
                  (< level 1))
       :actions [[:assoc-in
                  [(:id tool) pane-id :value :zoom/level]
                  (+ increment level)]]}
      {`protocols/render-toolbar-button #'Button})))

(def impl
  {`protocols/prepare-canvas (fn [_ el opt] (zoom el opt))
   `protocols/finalize-canvas (fn [_ _ _])
   `protocols/prepare-toolbar-button #'prepare-zoom-button})

(defn create-zoom-in-tool [config]
  (with-meta
    {:id :canvas/zoom
     :title "Zoom in"
     :icon plus/icon
     :zoom-increment (or (:zoom-increment config) 0.25)}
    impl))

(defn create-zoom-out-tool [config]
  (with-meta
    {:id :canvas/zoom
     :title "Zoom out"
     :icon minus/icon
     :zoom-increment (or (:zoom-increment config) -0.25)}
    impl))

(defn reset-canvas-zoom [data el opt]
  (when-not (contains? opt :zoom/level)
    (let [body (canvas/get-iframe-body el)]
      (set! (.. body -style -transform) "")
      (set! (.. body -style -width) "100%")
      (set! (.. body -style -height) "100%"))))

(defn create-reset-zoom-tool [config]
  (addons/create-action-button
   {:id :canvas/zoom-reset
    :title "Reset zoom"
    :icon reset/icon
    :prepare-canvas #'reset-canvas-zoom
    :get-actions (fn [_ _ {:keys [pane-id]}]
                   [[:dissoc-in [:canvas/zoom pane-id :value :zoom/level]]])
    :show? (fn [_ _ {:keys [pane-options]}]
             (and (:zoom/level pane-options)
                  (not= 1 (:zoom/level pane-options))))}))
