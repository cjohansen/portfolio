(ns portfolio.adapter)

(defprotocol IComponentLibrary
  :extend-via-metadata true
  (render-component [component el]))
