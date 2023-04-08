(ns portfolio.ui.canvas.compare
  (:require [portfolio.ui.canvas :refer [prepare-scenes]]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]
            [portfolio.ui.layout :as layout]
            [portfolio.ui.scene-browser :as scene-browser]))

(defn get-ctx [state view ctx id]
  (cond-> ctx
    id (assoc :scenes (->> (collection/get-selected-scenes state id)
                           (prepare-scenes state nil view nil)))))

(defn get-expand-path [id]
  [:panes id :menu-expanded?])

(defn can-curate? [state]
  (< 1 (count (layout/get-layout-panes (layout/get-current-layout state)))))

(defn create-compare-tool [_config]
  (with-meta
    {:id :canvas/selection}
    {`canvas/prepare-toolbar-button
     (fn [_tool state options]
       (when (can-curate? state)
         (let [path [:panes (:pane-id options) :curate-selection?]
               curating? (get-in state path)]
           (with-meta
             {:title "Select pane scene(s) for comparison"
              :icon :portfolio.ui.icons/git-diff
              :align :right
              :selected? curating?
              :actions (cond-> [[:assoc-in path (not curating?)]]
                         (not (contains? (get-in state [:panes (:pane-id options)]) :curate-selection?))
                         (conj [:assoc-in (get-expand-path (:pane-id options)) true]))}
             {`canvas/render-toolbar-button #'MenuButton}))))

     `canvas/prepare-pane
     (fn [_ f state view ctx]
       (let [expand-path (get-expand-path (:pane-id ctx))
             curating? (when (can-curate? state)
                         (get-in state [:panes (:pane-id ctx) :curate-selection?]))
             expanded? (and curating? (get-in state expand-path))
             id (when curating? (get-in state [:panes (:pane-id ctx) :selection-id]))
             ctx (if curating? (get-ctx state view ctx id) ctx)]
         (cond-> (f state view ctx)
           curating?
           (assoc :menu-bar (collection/prepare-selection-menu-bar
                             state
                             (if id
                               (collection/get-selection state id)
                               (:current-selection state))
                             {:expand-path expand-path
                              :tight? true}))

           expanded?
           (assoc :browser {:items
                            (scene-browser/prepare-browser
                             state
                             {:select-actions [[:assoc-in [:panes (:pane-id ctx) :selection-id] ::scene-browser/target-id]
                                               [:assoc-in expand-path false]]
                              :path-ctx [:panes (:pane-id ctx)]})})

           expanded?
           (dissoc :canvases))))}))
