(ns portfolio.ui.core
  (:require [clojure.walk :as walk]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.intro-screen :as intro]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene-browser :as scene-browser]
            [portfolio.ui.screen :as screen]
            [portfolio.ui.search :as search]
            [portfolio.ui.view :as view]))

(defn sidebar? [{:keys [sidebar-status] :as state}]
  (cond
    (= sidebar-status :hidden)
    false

    (= sidebar-status :visible)
    true

    :else
    (not (screen/small-screen? state))))

(defn prepare-scene-browser [state location & [{:keys [select-actions]}]]
  (->> {:path-ctx [:ui]
        :location location
        :select-actions (concat [[:event/prevent-default]
                                 [::navigate-to ::scene-browser/target-id]]
                                select-actions)
        :current-id (routes/get-id location)}
       (scene-browser/prepare-browser state)
       (walk/postwalk
        (fn [x]
          (if (and (vector? x) (= ::navigate-to (first x)))
            [:go-to-location (routes/get-location location {:id (second x)})]
            x)))))

(defn prepare-sidebar [state location]
  (when (sidebar? state)
    {:width 360
     :slide? (boolean (:sidebar-status state))
     :title (not-empty (:title state))
     :actions [[:assoc-in [:sidebar-status]
                (if (screen/small-screen? state)
                  :auto
                  :hidden)]]
     :items (prepare-scene-browser state location)
     :search (when (:index state)
               (search/prepare-search state location))}))

(defn prepare-header [state location]
  (when-not (sidebar? state)
    {:left-action
     (when-not (screen/small-screen? state)
       {:icon :portfolio.ui.icons/caret-double-right
        :actions [[:assoc-in [:sidebar-status]
                   (if (screen/small-screen? state)
                     :visible
                     :auto)]]})

     :menu-bar (collection/prepare-selection-menu-bar
                state
                (:current-selection state)
                {:expand-path [:header-menu-expanded?]
                 :location location
                 :tight? (screen/small-screen? state)})

     :menu
     (when (:header-menu-expanded? state)
       {:items (->> {:select-actions [[:assoc-in [:header-menu-expanded?] false]]}
                    (prepare-scene-browser state location))})}))

(defn get-current-view [state _location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn prepare-data [state location]
  (if (seq (vals (:scenes state)))
    (let [state (assoc state :current-selection (collection/get-selection state (routes/get-id location)))
          current-view (get-current-view state location)]
      {:header (prepare-header state location)
       :sidebar (prepare-sidebar state location)
       :small? (screen/small-screen? state)
       :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
       :view (view/prepare-data current-view state location)})
    {:view (intro/prepare-view state location)}))
