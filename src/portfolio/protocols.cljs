(ns portfolio.protocols)

(defprotocol IViewData
  :extend-via-metadata true
  (prepare-data [view state location]))

(defprotocol IView
  :extend-via-metadata true
  (render-view [self]))

(defprotocol ICanvasTool
  :extend-via-metadata true
  (prepare-layer [self el opt])
  (get-local-overrides [self state canvas-id]))

(defprotocol ICanvasFinalizer
  :extend-via-metadata true
  (finalize-layer [self el opt]))

(defprotocol ICanvasAddon
  :extend-via-metadata true
  (prepare-addon-content [panel state location canvas]))

(extend-type cljs.core/PersistentArrayMap
  ICanvasFinalizer
  (finalize-layer [data el opt])

  IViewData
  (prepare-data [view state location]
    view)

  ICanvasTool
  (get-local-overrides [tool state canvas-id]
    (get-in state [(:id tool) canvas-id :value])))
