(ns portfolio.views.canvas.split
  (:require [portfolio.components.canvas-toolbar-buttons :refer [Button]]
            [portfolio.views.canvas.protocols :as protocols]
            [portfolio.icons.rows :as rows]
            [portfolio.icons.columns :as columns]
            [portfolio.icons.cross :as cross]))

(defn split-layout [layout path dir]
  (if (= 1 (count path))
    (let [[x] path]
      (cond-> layout
        (= dir (:kind layout))
        (update :xs (fn [xs]
                      (vec (mapcat (fn [col i]
                                     (if (= i x)
                                       [col col]
                                       [col]))
                                   xs (range)))))

        (not= dir (:kind layout))
        (update :xs (fn [xs]
                      (mapv (fn [col i]
                              (if (= i x)
                                {:kind dir
                                 :xs [col col]}
                                col))
                            xs (range))))))
    (update-in layout [:xs (first path)] split-layout (rest path) dir)))

(defn split-layout-horizontally [layout path]
  (split-layout layout path :rows))

(defn prepare-horizontal-split-button [tool state {:keys [pane-path layout-path layout]}]
  (with-meta
    {:title (:title tool)
     :icon rows/icon
     :actions [[:assoc-in layout-path
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
  (split-layout layout path :cols))

(defn prepare-vertical-split-button [tool state {:keys [pane-path layout-path layout]}]
  (with-meta
    {:title (:title tool)
     :icon columns/icon
     :actions [[:assoc-in layout-path
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
    (update layout :xs (fn [xs]
                         (vec (concat
                               (take (first path) xs)
                               (drop (inc (first path)) xs)))))
    (update-in layout [:xs (first path)] close-pane (rest path))))

(defn prepare-close-pane-button [tool state {:keys [pane-path layout-path layout]}]
  (when (< 1 (->> layout
                  (tree-seq coll? identity)
                  (filter map?)
                  (remove :kind)
                  count))
    (with-meta
      {:title (:title tool)
       :icon cross/icon
       :align :right
       :actions [[:assoc-in layout-path
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
