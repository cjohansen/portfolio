(ns portfolio.ui.scene-browser
  (:require [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]))

(defn get-expanded-path [collection]
  [:ui (:id collection) :expanded?])

(defn get-context [{:keys [collections]} path]
  (map (comp :kind collections) path))

(declare prepare-collections)

(defn prepare-package [state location collection path]
  (let [exp-path (get-expanded-path collection)
        expanded? (get-in state exp-path)]
    (cond-> {:title (:title collection)
             :kind :package
             :context (get-context state path)
             :actions [[:go-to-location (assoc-in location [:query-params :id] (:id collection))]]
             :illustration (collection/get-package-illustration state collection expanded?)
             :toggle {:icon (if expanded?
                              :ui.icons/caret-down
                              :ui.icons/caret-right)
                      :actions [[:assoc-in exp-path (not expanded?)]]}}
      expanded?
      (assoc :items (prepare-collections state location (conj path (:id collection))))

      (= (:id collection) (routes/get-id location))
      (assoc :selected? true))))

(defn prepare-folder [state location collection path]
  (let [exp-path (get-expanded-path collection)
        ;; Folders are expanded by default
        expanded? (not= false (get-in state exp-path))]
    (cond-> {:title (:title collection)
             :kind :folder
             :context (get-context state path)
             :actions [[:assoc-in exp-path (not expanded?)]]
             :illustration (collection/get-folder-illustration state collection expanded?)}
      expanded?
      (assoc :items (prepare-collections state location (conj path (:id collection)))))))

(defn prepare-scene [state location scene path]
  (let [selected? (= (:id scene) (routes/get-id location))]
    (cond-> {:title (:title scene)
             :kind :item
             :illustration (collection/get-scene-illustration state scene selected?)
             :context (get-context state path)
             :url (routes/get-scene-url location scene)}
      selected? (assoc :selected? true))))

(defn prepare-collections [state location & [parent-ids]]
  (let [parent-ids (or parent-ids [])]
    (for [item (concat
                (->> (vals (:collections state))
                     (filter (collection/by-parent-id (last parent-ids)))
                     (sort-by collection/get-sort-key))
                (->> (vals (:scenes state))
                     (filter (collection/by-parent-id (last parent-ids)))
                     (sort-by scene/sort-key)))]
      (case (:kind item)
        :package (prepare-package state location item parent-ids)
        :folder (prepare-folder state location item parent-ids)
        (prepare-scene state location item parent-ids)))))
