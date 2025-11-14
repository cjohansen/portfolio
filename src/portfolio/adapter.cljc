(ns portfolio.adapter)

(defprotocol IComponentLibrary
  :extend-via-metadata true
  (render-component [component el]))

(defn prepare-scene [scene component-impl]
  (with-meta
    scene
    component-impl))
