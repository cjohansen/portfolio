(ns portfolio.ui.canvas.selection
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

(defn create-selection-tool [_config]
  (with-meta
    {:id :canvas/selection}
    {`canvas/prepare-toolbar-button
     (fn [_tool state options]
       (when (< 1 (count (layout/get-layout-panes (layout/get-current-layout state))))
         (let [path [:panes (:pane-id options) :curate-selection?]
               curating? (get-in state path)]
           (with-meta
             {:title "Select pane scene(s)"
              :icon :portfolio.ui.icons/list-plus
              :align :right
              :selected? curating?
              :actions [[:assoc-in path (not curating?)]]}
             {`canvas/render-toolbar-button #'MenuButton}))))
     `canvas/prepare-pane
     (fn [_ f state view ctx]
       (let [expand-path [:panes (:pane-id ctx) :menu-expanded?]
             curating? (get-in state [:panes (:pane-id ctx) :curate-selection?])
             expanded? (and curating? (get-in state expand-path))
             id (when curating? (get-in state [:panes (:pane-id ctx) :selection-id]))]
         (cond-> (f state view (get-ctx state view ctx id))
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
