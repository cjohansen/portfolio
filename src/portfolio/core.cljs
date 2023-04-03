(ns portfolio.core
  (:require [portfolio.code :as code]
            [portfolio.collection :as collection]
            [portfolio.screen :as screen]
            [portfolio.sidebar :as sidebar]
            [portfolio.view :as view]
            [portfolio.routes :as routes]))

(defn get-current-view [state location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn prepare-header [state _]
  (when-not (sidebar/sidebar? state)
    {:title (not-empty (:title state))
     :actions [[:assoc-in [:sidebar-status]
                (if (screen/small-screen? state)
                  :visible
                  :auto)]]}))

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn get-scene-param-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-scene-param [state scene]
  (if (map? (:param scene))
    (merge (:param scene) (get-scene-param-overrides state scene))
    (:param scene)))

(defn prep-scene-fns [state scenes]
  (for [scene scenes]
    (let [param (get-scene-param state scene)]
      (cond-> (assoc scene :component-param (code/code-str param))
        (:component scene)
        (assoc :component-fn #(:component scene))

        (:component-fn scene)
        (assoc :component-fn #((:component-fn scene) param %))))))

(defn prepare-data [state location]
  (let [selection (collection/get-selection state (routes/get-id location))
        current-scenes (->> (:scenes selection)
                            (prep-scene-fns state)
                            (sort-by :idx))
        current-view (get-current-view state location)]
    {:header (prepare-header state location)
     :sidebar (sidebar/prepare-sidebar state location)
     :small? (screen/small-screen? state)
     :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
     :view (view/prepare-data current-view (assoc state :current-scenes current-scenes) location)}))
