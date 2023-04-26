(ns portfolio.ui.canvas.code
  (:require [phosphor.icons :as icons]
            [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]))

(defn show-code? [state tool opts]
  (-> (addons/get-current-value state tool opts)
      (get :code/show? false)))

(defn create-code-tool [config]
  (let [tool {:id :canvas/code
              :global? (:docs/global-toggle? config true)
              :persist? (:docs/global-toggle? config true)}]
    (with-meta
      tool
      {`canvas/prepare-toolbar-button
       (fn [_tool state options]
         (let [code? (show-code? state tool options)]
           (when (->> (vals (:scenes state))
                      (concat (vals (:collections state)))
                      (keep :code)
                      seq)
             (with-meta
               {:title "Toggle code"
                :button-group :canvas/docs
                :icon (icons/icon :phosphor.regular/brackets-square)
                :selected? code?
                :actions (addons/get-set-actions state tool (:pane-id options) {:code/show? (not code?)})}
               {`canvas/render-toolbar-button #'MenuButton}))))

       `canvas/prepare-pane
       (fn [_ f state view ctx]
         (f
          state
          view
          (cond-> ctx
            (not (show-code? state tool ctx))
            (update :scenes (fn [scenes] (map #(dissoc % :code) scenes))))))

       `canvas/prepare-view
       (fn [_ f state location view]
         (cond-> (f state location view)
           (not (show-code? state tool {:pane-id (:id view)}))
           (dissoc :code)))})))
