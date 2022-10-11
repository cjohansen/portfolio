(ns portfolio.views.canvas.background
  (:require [portfolio.components.canvas :as canvas]
            [portfolio.views.canvas.addons :as addons]))

(defn prepare-layer [data el {:background/keys [background-color body-class]}]
  (set! (.. (canvas/get-iframe el) -style -backgroundColor) background-color)
  (set! (.-className (canvas/get-iframe-body el)) (or body-class "")))

(defn create-background-tool [config]
  (addons/create-toolbar-menu-button
   {:id :canvas/background
    :title "Background"
    :options [{:id :light
               :title "Light (.light-bg)"
               :value {:background/background-color "#f8f8f8"
                       :background/body-class "light-bg"}}
              {:id :dark
               :title "Dark (body.dark-bg)"
               :value {:background/background-color "#111111"
                       :background/body-class "dark-bg"}}]
    :prepare-layer #'prepare-layer}))
