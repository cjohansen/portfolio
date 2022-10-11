(ns portfolio.views.canvas.protocols)

(defprotocol ICanvasToolbarButtonData
  :extend-via-metadata true
  (prepare-toolbar-button [self state opt]))

(defprotocol ICanvasToolbarButtonView
  :extend-via-metadata true
  (render-toolbar-button [data]))
