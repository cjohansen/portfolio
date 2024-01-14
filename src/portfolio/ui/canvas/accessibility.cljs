(ns portfolio.ui.canvas.accessibility
  (:require [phosphor.icons :as icons]
            [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]))

(defn assess-accessibility? [state tool opts]
  (-> (addons/get-current-value state tool opts)
      (get :accessibility/show? true)))

(def scripts
  ["/portfolio/axe.min.js"
   "/portfolio/accessibility.js"])

(defn create-accessibility-tool [_config]
  (let [tool {:id :canvas/accessibility
              :global? true
              :persist? true}]
    (with-meta
      tool
      {`canvas/prepare-toolbar-button
       (fn [_tool state options]
         (let [engaged? (assess-accessibility? state tool options)]
           (when (->> (vals (:scenes state))
                      (concat (vals (:collections state)))
                      (keep :docs)
                      seq)
             (with-meta
               {:title "Toggle docs"
                :button-group :canvas/docs
                :icon (icons/icon :phosphor.regular/person-arms-spread)
                :selected? engaged?
                :actions (addons/get-set-actions state tool (:pane-id options) {:accessibility/show? (not engaged?)})}
               {`canvas/render-toolbar-button #'MenuButton}))))

       `canvas/prepare-pane
       (fn [_ f state view ctx]
         (f
          state
          view
          (cond-> ctx
            (assess-accessibility? state tool ctx)
            (update :scenes (fn [scenes] (map #(update % :script-paths into scripts) scenes))))))

       `canvas/prepare-view
       (fn [_ f state location view]
         ;; TODO: Something S-M-R-T
         (f state location view)
         #_(cond-> (f state location view)
           (not (assess-accessibility? state tool {:pane-id (:id view)}))
           (dissoc :title :description)))})))
