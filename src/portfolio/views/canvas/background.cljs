(ns portfolio.views.canvas.background
  (:require [portfolio.components.canvas :as canvas]
            [portfolio.protocols :as portfolio]))

(def impl
  {`portfolio/prepare-layer
   (fn [data el {:background/keys [background-color body-class]} wrapper]
     (set! (.. (canvas/get-iframe el) -style -backgroundColor) background-color)
     (set! (.-className (canvas/get-iframe-body el)) (or body-class "")))})

(defn create-background-tool [config]
  (with-meta
    {:id :canvas/background
     :title "Background"
     :options [{:id :light
                :title "Light (.light-bg)"
                :value {:background/background-color "#f8f8f8"
                        :background/body-class "light-bg"}}
               {:id :dark
                :title "Dark (body.dark-bg)"
                :value {:background/background-color "#111111"
                        :background/body-class "dark-bg"}}]}
    impl))
