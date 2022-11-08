(ns portfolio.views.canvas.split
  (:require [portfolio.components.canvas-toolbar-buttons :refer [Button]]
            [portfolio.views.canvas.protocols :as protocols]))

(defn split-layout-horizontally [layout path]
  (let [x (last path)]
    (update-in layout (drop-last 1 path)
               (fn [cols]
                 (mapcat (fn [col x*]
                           (if (= x* x)
                             [col col]
                             [col]))
                         cols (range))))))

(defn prepare-zoom-button [tool state {:keys [pane-path layout-path layout]}]
  (with-meta
    {:text (:title tool)
     :active? false
     :actions [[:assoc-in layout-path
                (split-layout-horizontally layout pane-path)]]}
    {`protocols/render-toolbar-button #'Button}))

(def impl
  {`protocols/prepare-canvas (fn [_ el opt])
   `protocols/prepare-toolbar-button #'prepare-zoom-button})

(defn create-split-horizontal-tool [config]
  (with-meta
    {:id :canvas/split-horizontal
     :title "Split horizontal"
     }
    impl))
