(ns portfolio.ui.canvas.docs
  (:require [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]))

(defn get-path [config pane-id]
  (if (:docs/global-toggle? config true)
    [:docs/show-docs?]
    [:panes pane-id :show-docs?]))

(defn create-docs-tool [config]
  (with-meta
    {:id :canvas/selection}
    {`canvas/prepare-toolbar-button
     (fn [_tool state options]
       (let [path (get-path config (:pane-id options))
             docs? (get-in state path true)]
         (when (->> (vals (:scenes state))
                    (concat (vals (:collections state)))
                    (keep :docs)
                    seq)
           (with-meta
             {:title "Toggle docs"
              :icon :portfolio.ui.icons/file-doc
              :selected? docs?
              :actions [[:assoc-in path (not docs?)]]}
             {`canvas/render-toolbar-button #'MenuButton}))))

     `canvas/prepare-pane
     (fn [_ f state view ctx]
       (f
        state
        view
        (cond-> ctx
          (not (get-in state (get-path config (:pane-id ctx)) true))
          (update :scenes (fn [scenes] (map #(dissoc % :title :description) scenes))))))

     `canvas/prepare-view
     (fn [_ f state location view]
       (cond-> (f state location view)
         (not (get-in state (get-path config (:id view)) true))
         (dissoc :title :description)))}))
