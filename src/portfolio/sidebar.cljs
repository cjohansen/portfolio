(ns portfolio.sidebar
  (:require [portfolio.collection :as collection]
            [portfolio.routes :as routes]
            [portfolio.screen :as screen]))

(defn get-expanded-path [collection]
  [:ui (:id collection) :expanded?])

(defn sidebar? [{:keys [sidebar-status] :as state}]
  (cond
    (= sidebar-status :hidden)
    false

    (= sidebar-status :visible)
    true

    :else
    (not (screen/small-screen? state))))

(defn get-context [{:keys [collections]} path]
  (map (comp :kind collections) path))

(declare prepare-collections)

(defn get-package-illustration [expanded? collection]
  {:icon (or (if expanded?
               (:expanded-icon collection)
               (:collapsed-icon collection))
             (:icon collection)
             :ui.icons/cube)
   :color (or (if expanded?
                (:expanded-icon-color collection)
                (:collapsed-icon-color collection))
              (:icon-color collection)
              "var(--azure-radiance)")})

(defn prepare-package [state location collection path]
  (let [exp-path (get-expanded-path collection)
        expanded? (get-in state exp-path)]
    (cond-> {:title (:title collection)
             :kind :togglable
             :context (get-context state path)
             :actions [[:go-to-location (assoc-in location [:query-params :id] (:id collection))]]
             :illustration (get-package-illustration expanded? collection)
             :toggle {:icon (if expanded?
                              :ui.icons/caret-down
                              :ui.icons/caret-right)
                      :actions [[:assoc-in exp-path (not expanded?)]]}}
      expanded?
      (assoc :items (prepare-collections state location (conj path (:id collection)))))))

(defn get-folder-illustration [expanded? collection]
  {:icon (or (if expanded?
               (:expanded-icon collection)
               (:collapsed-icon collection))
             (:icon collection)
             (if expanded?
               :ui.icons/folder-open
               :ui.icons/folder))
   :color (or (if expanded?
                (:expanded-icon-color collection)
                (:collapsed-icon-color collection))
              (:icon-color collection)
              "var(--cadet-blue)")})

(defn prepare-folder [state location collection path]
  (let [exp-path (get-expanded-path collection)
        ;; Folders are expanded by default
        expanded? (not= false (get-in state exp-path))]
    (cond-> {:title (:title collection)
             :kind :folder
             :context (get-context state path)
             :actions [[:assoc-in exp-path (not expanded?)]]
             :illustration (get-folder-illustration expanded? collection)}
      expanded?
      (assoc :items (prepare-collections state location (conj path (:id collection)))))))

(defn get-scene-illustration [selected? scene]
  {:icon (or (when selected?
               (:selected-icon scene))
             (:icon scene)
             :ui.icons/bookmark)
   :color (or (when selected?
                (:selected-icon-color scene))
              (:icon-color scene)
              (when-not selected?
                "var(--silver-tree)"))})

(defn prepare-scene [state location scene path]
  (let [selected? (= (:id scene) (routes/get-id location))]
    (cond-> {:title (:title scene)
             :kind :item
             :illustration (get-scene-illustration selected? scene)
             :context (get-context state path)
             :url (routes/get-scene-url location scene)}
      selected? (assoc :selected? true))))

(defn prepare-collections [state location parent-ids]
  (for [item (concat
              (->> (vals (:collections state))
                   (filter (collection/by-parent-id (last parent-ids)))
                   (sort-by collection/get-sort-key))
              (->> (vals (:scenes state))
                   (filter (collection/by-parent-id (last parent-ids)))
                   (sort-by (juxt :line :idx))))]
    (case (:kind item)
      :package (prepare-package state location item parent-ids)
      :folder (prepare-folder state location item parent-ids)
      (prepare-scene state location item parent-ids))))

(defn prepare-sidebar [state location]
  (when (sidebar? state)
    {:width 360
     :slide? (boolean (:sidebar-status state))
     :title (not-empty (:title state))
     :actions [[:assoc-in [:sidebar-status]
                (if (screen/small-screen? state)
                  :auto
                  :hidden)]]
     :items (prepare-collections state location [])}))
