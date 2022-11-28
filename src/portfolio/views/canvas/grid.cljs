(ns portfolio.views.canvas.grid
  (:require [clojure.string :as str]
            [portfolio.components.canvas :as canvas]
            [portfolio.views.canvas.addons :as addons]))

(defn prepare-canvas [data el {:grid/keys [offset size group-size] :as opt}]
  (let [body-style (.-style (canvas/get-iframe el))
        zoom (or (:zoom/level opt) 1)]
    (if (and (number? size) (not= 0 size))
      (let [real-size (* zoom size)]
        (set! (.-backgroundSize body-style)
              (let [big (* (or group-size 5) real-size)]
                (str big "px " big "px, " big "px " big "px, "
                     real-size "px " real-size "px, " real-size "px " real-size "px")))
        (set! (.-backgroundPosition body-style)
              (let [offset (- (or offset 0) (- real-size size))]
                (str/join ", " (repeat 4 (str offset "px " 0 "px")))))
        (set! (.-backgroundBlendMode body-style) "difference")
        (set! (.-backgroundImage body-style)
              "linear-gradient(rgba(130, 130, 130, 0.5) 1px, transparent 1px),
                  linear-gradient(90deg, rgba(130, 130, 130, 0.5) 1px, transparent 1px),
                  linear-gradient(rgba(130, 130, 130, 0.25) 1px, transparent 1px),
                  linear-gradient(90deg, rgba(130, 130, 130, 0.25) 1px, transparent 1px)"))
      (set! (.-backgroundImage body-style) "none"))))

(defn create-grid-tool [config]
  (addons/create-toolbar-menu-button
   {:id :canvas/grid
    :title "Grid"
    :options [{:title "5 x 20px"
               :value {:grid/offset 0
                       :grid/size 20
                       :grid/group-size 5}}
              {:title "No grid"
               :value {:grid/size 0}}]
    :prepare-canvas #'prepare-canvas}))
