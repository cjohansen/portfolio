(ns portfolio.ui.core
  (:require [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.screen :as screen]
            [portfolio.ui.view :as view]
            [portfolio.ui.scene-browser :as scene-browser]))

(defn sidebar? [{:keys [sidebar-status] :as state}]
  (cond
    (= sidebar-status :hidden)
    false

    (= sidebar-status :visible)
    true

    :else
    (not (screen/small-screen? state))))

(defn prepare-sidebar [state location]
  (when (sidebar? state)
    {:width 360
     :slide? (boolean (:sidebar-status state))
     :title (not-empty (:title state))
     :actions [[:assoc-in [:sidebar-status]
                (if (screen/small-screen? state)
                  :auto
                  :hidden)]]
     :items (scene-browser/prepare-collections state location)}))

(defn prepare-header [state location]
  (when-not (sidebar? state)
    (let [current (-> state :current-selection :target)]
      {:illustration (some-> current (collection/get-illustration state))
       :title (if (screen/small-screen? state)
                [{:text (:title current)}]
                (for [item (-> state :current-selection :path)]
                  (cond-> {:text (:title item)}
                    (not= current item)
                    (assoc :url (routes/get-url location item)))))
       :left-action (when-not (screen/small-screen? state)
                      {:icon :ui.icons/caret-double-right
                       :actions [[:assoc-in [:sidebar-status]
                                  (if (screen/small-screen? state)
                                    :visible
                                    :auto)]]})
       :right-action {:icon (if (:header-menu-expanded? state)
                              :ui.icons/caret-up
                              :ui.icons/caret-down)
                      :actions [[:assoc-in [:header-menu-expanded?]
                                 (not (:header-menu-expanded? state))]]}
       :menu (when (:header-menu-expanded? state)
               {:items (scene-browser/prepare-collections state location)})})))

(defn get-current-view [state _location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn prepare-data [state location]
  (let [state (assoc state :current-selection (collection/get-selection state (routes/get-id location)))
        current-view (get-current-view state location)]
    {:header (prepare-header state location)
     :sidebar (prepare-sidebar state location)
     :small? (screen/small-screen? state)
     :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
     :view (view/prepare-data current-view state location)}))
