(ns portfolio.ui.core
  (:require [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]
            [portfolio.ui.screen :as screen]
            [portfolio.ui.sidebar :as sidebar]
            [portfolio.ui.view :as view]))

(defn prepare-header [state _]
  (when-not (sidebar/sidebar? state)
    {:title (not-empty (:title state))
     :actions [[:assoc-in [:sidebar-status]
                (if (screen/small-screen? state)
                  :visible
                  :auto)]]}))

(defn get-current-view [state location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn prepare-data [state location]
  (let [selection (collection/get-selection state (routes/get-id location))
        current-scenes (scene/prepare-scenes state (:scenes selection))
        current-view (get-current-view state location)]
    {:header (prepare-header state location)
     :sidebar (sidebar/prepare-sidebar state location)
     :small? (screen/small-screen? state)
     :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
     :view (view/prepare-data current-view (assoc state :current-scenes current-scenes) location)}))
