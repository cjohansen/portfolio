(ns portfolio.kitchen-sink
  "Use this namespace to quickly mount a Portfolio UI with the default setup."
  (:require [portfolio.client :as client]
            [portfolio.core :as portfolio]
            [portfolio.views.canvas :as canvas]
            [portfolio.views.canvas.args :as canvas-args]
            [portfolio.views.canvas.background :as canvas-bg]
            [portfolio.views.canvas.grid :as canvas-grid]
            [portfolio.views.canvas.viewport :as canvas-vp]
            [portfolio.views.canvas.zoom :as canvas-zoom]))

(defn create-app [config]
  (-> (portfolio/init-state config)
      (assoc :views [(canvas/create-canvas
                      {:canvas/layout (:canvas/layout config)
                       :tools [(canvas-bg/create-background-tool config)
                               (canvas-vp/create-viewport-tool config)
                               (canvas-grid/create-grid-tool config)
                               (canvas-zoom/create-zoom-in-tool config)
                               (canvas-zoom/create-zoom-out-tool config)
                               (canvas-zoom/create-reset-zoom-tool config)]
                       :addons [(canvas-args/create-args-panel config)]})])
      atom))

(defn start! [config & opt]
  (client/start-app (create-app config) opt))
