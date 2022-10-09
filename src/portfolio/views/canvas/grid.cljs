(ns portfolio.views.canvas.grid
  (:require [clojure.string :as str]
            [portfolio.protocols :as portfolio]
            [portfolio.components.canvas :as canvas]))

(def impl
  {`portfolio/prepare-layer
   (fn [data el {:grid/keys [offset size group-size]}]
     (let [body-style (.-style (canvas/get-iframe el))]
       (if (and (number? size) (not= 0 size))
         (do
           (set! (.-backgroundSize body-style)
                 (let [big (* (or group-size 5) size)]
                   (str big "px " big "px, " big "px " big "px, "
                        size "px " size "px, " size "px " size "px")))
           (set! (.-backgroundPosition body-style)
                 (let [offset (or offset 0)]
                   (str/join ", " (repeat 4 (str offset "px " offset "px")))))
           (set! (.-backgroundBlendMode body-style) "difference")
           (set! (.-backgroundImage body-style)
                 "linear-gradient(rgba(130, 130, 130, 0.5) 1px, transparent 1px),
                  linear-gradient(90deg, rgba(130, 130, 130, 0.5) 1px, transparent 1px),
                  linear-gradient(rgba(130, 130, 130, 0.25) 1px, transparent 1px),
                  linear-gradient(90deg, rgba(130, 130, 130, 0.25) 1px, transparent 1px)"))
         (set! (.-backgroundImage body-style) "none"))))})

(defn create-grid-tool [config]
  (with-meta
    {:id :canvas/grid
     :title "Grid"
     :options [{:title "5 x 20px"
                :value {:grid/offset 0
                        :grid/size 20
                        :grid/group-size 5}}
               {:title "No grid"
                :value {:grid/size 0}}]}
    impl))
