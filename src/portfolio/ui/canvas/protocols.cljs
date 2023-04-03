(ns portfolio.ui.canvas.protocols)

(defprotocol ICanvasToolbarButtonData
  :extend-via-metadata true
  (prepare-toolbar-button [self state opt]))

(defprotocol ICanvasToolbarButtonView
  :extend-via-metadata true
  (render-toolbar-button [data]))

(defprotocol ICanvasTool
  :extend-via-metadata true
  (prepare-canvas [self el opt])
  (finalize-canvas [self el opt]))

(defprotocol ICanvasToolValue
  :extend-via-metadata true
  (get-tool-value [self state canvas-id]))

(defprotocol ICanvasPanelAddon
  :extend-via-metadata true
  (prepare-panel-content [panel state scene]))
