(ns portfolio.ui.canvas.docs
  (:require [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]))

(defn show-docs? [state tool opts]
  (-> (addons/get-current-value state tool opts)
      (get :docs/show? true)))

(defn create-docs-tool [config]
  (let [tool {:id :canvas/docs
              :global? (:docs/global-toggle? config true)
              :persist? (:docs/global-toggle? config true)}]
    (with-meta
      tool
      {`canvas/prepare-toolbar-button
       (fn [_tool state options]
         (let [docs? (show-docs? state tool options)]
           (when (->> (vals (:scenes state))
                      (concat (vals (:collections state)))
                      (keep :docs)
                      seq)
             (with-meta
               {:title "Toggle docs"
                :icon :portfolio.ui.icons/file-doc
                :selected? docs?
                :actions (addons/get-set-actions tool (:pane-id options) {:docs/show? (not docs?)})}
               {`canvas/render-toolbar-button #'MenuButton}))))

       `canvas/prepare-pane
       (fn [_ f state view ctx]
         (f
          state
          view
          (cond-> ctx
            (not (show-docs? state tool ctx))
            (update :scenes (fn [scenes] (map #(dissoc % :title :description) scenes))))))

       `canvas/prepare-view
       (fn [_ f state location view]
         (cond-> (f state location view)
           (not (show-docs? state tool {:pane-id (:id view)}))
           (dissoc :title :description)))})))
