(ns mirror.scenes
  (:require [dataspex.core :as dataspex]
            [mirror.ui.colors-scenes]
            [mirror.ui.components.auto-complete-scenes]
            [mirror.ui.components.browser-scenes]
            [mirror.ui.components.document-scenes]
            [mirror.ui.components.hud-scenes]
            [portfolio.ui :as ui]))

::mirror.ui.components.browser-scenes/keep
::mirror.ui.colors-scenes/keep
::mirror.ui.components.auto-complete-scenes/keep
::mirror.ui.components.document-scenes/keep
::mirror.ui.components.hud-scenes/keep

(def app
  (ui/start!
   {:on-render (fn [page-data]
                 (dataspex/inspect "Page data" page-data))
    :config
    {:css-paths ["/portfolio/styles/portfolio.css"]
     :viewport/defaults {:viewport/padding [16]}}}))

(dataspex/inspect "Application data" app)
