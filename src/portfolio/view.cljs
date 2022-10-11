(ns portfolio.view)

(defprotocol IViewData
  :extend-via-metadata true
  (prepare-data [view state location]))

(defprotocol IView
  :extend-via-metadata true
  (render-view [self]))
