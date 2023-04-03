(ns portfolio.ui.canvas.split
  (:require [portfolio.ui.canvas.protocols :as protocols]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [Button]]
            [portfolio.ui.layout :as layout]))

(def complement-dir
  {:cols :rows
   :rows :cols})

(defn split-layout [layout path dir]
  (cond
    (= 0 (count path))
    {:kind dir
     :xs [layout (layout/assign-pane-ids layout)]}

    (= 1 (count path))
    (let [[x] path]
      (cond-> layout
        (= dir (:kind layout))
        (update :xs (fn [xs]
                      (vec (mapcat (fn [col i]
                                     (if (= i x)
                                       [col (layout/assign-pane-ids col)]
                                       [col]))
                                   xs (range)))))

        (not= dir (:kind layout))
        (update :xs (fn [xs]
                      (mapv (fn [col i]
                              (if (= i x)
                                {:kind dir
                                 :xs [col (layout/assign-pane-ids col)]}
                                col))
                            xs (range))))))

    :else
    (update-in layout [:xs (first path)] split-layout (rest path) dir)))

(defn split-layout-horizontally [layout path]
  (split-layout layout path :cols))

(defn prepare-horizontal-split-button [tool _state {:keys [pane-path layout-path layout]}]
  (with-meta
    {:title (:title tool)
     :icon :ui.icons/columns
     :actions [[:assoc-in (into layout-path [:layout])
                (split-layout-horizontally layout pane-path)]]}
    {`protocols/render-toolbar-button #'Button}))

(def horizontal-impl
  {`protocols/prepare-canvas (fn [_ el opt])
   `protocols/prepare-toolbar-button #'prepare-horizontal-split-button})

(defn create-split-horizontally-tool [config]
  (with-meta
    {:id :canvas/split-horizontally
     :title "Split horizontally"}
    horizontal-impl))

(defn split-layout-vertically [layout path]
  (split-layout layout path :rows))

(defn prepare-vertical-split-button [tool state {:keys [pane-path layout-path layout]}]
  (with-meta
    {:title (:title tool)
     :icon :ui.icons/rows
     :actions [[:assoc-in (into layout-path [:layout])
                (split-layout-vertically layout pane-path)]]}
    {`protocols/render-toolbar-button #'Button}))

(def vertical-impl
  {`protocols/prepare-canvas (fn [_ el opt])
   `protocols/prepare-toolbar-button #'prepare-vertical-split-button})

(defn create-split-vertically-tool [config]
  (with-meta
    {:id :canvas/split-vertically
     :title "Split vertically"}
    vertical-impl))

(defn close-pane [layout path]
  (if (= 1 (count path))
    (let [xs (vec (concat
                   (take (first path) (:xs layout))
                   (drop (inc (first path)) (:xs layout))))]
      (if (= 1 (count xs))
        (first xs)
        (assoc layout :xs xs)))
    (update-in layout [:xs (first path)] close-pane (rest path))))

(defn prepare-close-pane-button [tool _state {:keys [pane-path layout-path layout] :as lol}]
  (when (and (< 1 (->> layout
                       (tree-seq coll? identity)
                       (filter map?)
                       (remove :kind)
                       count))
             (< 0 (count pane-path)))
    (with-meta
      {:title (:title tool)
       :icon :ui.icons/cross
       :align :right
       :actions [[:assoc-in (into layout-path [:layout])
                  (close-pane layout pane-path)]]}
      {`protocols/render-toolbar-button #'Button})))

(def close-impl
  {`protocols/prepare-canvas (fn [_ el opt])
   `protocols/prepare-toolbar-button #'prepare-close-pane-button})

(defn create-close-tool [config]
  (with-meta
    {:id :canvas/close
     :title "Close pane"}
    close-impl))
