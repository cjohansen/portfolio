(ns mirror.scenes
  (:require [gadget.inspector :as inspector]
            [mirror.ui.components.auto-complete-scenes]
            [mirror.ui.components.browser-scenes]
            [mirror.ui.colors-scenes]
            [mirror.ui.components.hud-scenes]
            [portfolio.ui :as ui]))

::mirror.ui.components.browser-scenes/keep
::mirror.ui.colors-scenes/keep
::mirror.ui.components.auto-complete-scenes/keep
::mirror.ui.components.hud-scenes/keep

(def app
  (ui/start!
   {:on-render (fn [page-data]
                 (inspector/inspect "Page data" page-data))
    :config
    {:css-paths ["/portfolio/styles/portfolio.css"]
     :viewport/defaults {:viewport/padding [16]}}}))

(inspector/inspect "Application data" app)
