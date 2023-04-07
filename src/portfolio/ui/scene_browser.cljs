(ns portfolio.ui.scene-browser
  (:require [clojure.walk :as walk]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]))

(defn get-expanded-path
  ([collection]
   (get-expanded-path {:path-ctx [:ui]} collection))
  ([{:keys [path-ctx]} collection]
   (into path-ctx [(:id collection) :expanded?])))

(defn get-context [{:keys [collections]} path]
  (map (comp :kind collections) path))

(declare prepare-browser-collections)

(defn get-actions [opt target]
  (walk/postwalk
   #(if (= ::target-id %)
      (:id target)
      %)
   (:select-actions opt)))

(defn prepare-package [state opt collection path]
  (let [exp-path (get-expanded-path opt collection)
        expanded? (get-in state exp-path)]
    (cond-> {:title (:title collection)
             :kind :package
             :context (get-context state path)
             :actions (get-actions opt collection)
             :illustration (collection/get-package-illustration state collection expanded?)
             :toggle {:icon (if expanded?
                              :portfolio.ui.icons/caret-down
                              :portfolio.ui.icons/caret-right)
                      :actions [[:assoc-in exp-path (not expanded?)]]}}
      expanded?
      (assoc :items (prepare-browser-collections state opt (conj path (:id collection))))

      (= (:id collection) (:current-id opt))
      (assoc :selected? true))))

(defn prepare-folder [state opt collection path]
  (let [exp-path (get-expanded-path opt collection)
        ;; Folders are expanded by default
        expanded? (not= false (get-in state exp-path))]
    (cond-> {:title (:title collection)
             :kind :folder
             :context (get-context state path)
             :actions [[:assoc-in exp-path (not expanded?)]]
             :illustration (collection/get-folder-illustration state collection expanded?)}
      expanded?
      (assoc :items (prepare-browser-collections state opt (conj path (:id collection)))))))

(defn prepare-scene [state opt scene path]
  (let [selected? (= (:id scene) (:current-id opt))]
    (cond-> {:title (:title scene)
             :kind :item
             :illustration (collection/get-scene-illustration state scene selected?)
             :context (get-context state path)
             :actions (get-actions opt scene)}
      (:location opt) (assoc :url (routes/get-scene-url (:location opt) scene))
      selected? (assoc :selected? true))))

(defn prepare-browser-collections [state opt parent-ids]
  (for [item (concat
              (->> (vals (:collections state))
                   (filter (collection/by-parent-id (last parent-ids)))
                   (sort-by collection/get-sort-key))
              (->> (vals (:scenes state))
                   (filter (collection/by-parent-id (last parent-ids)))
                   (sort-by scene/sort-key)))]
    (case (:kind item)
      :package (prepare-package state opt item parent-ids)
      :folder (prepare-folder state opt item parent-ids)
      (prepare-scene state opt item parent-ids))))

(defn prepare-browser
  "`opt` is a map of:

  - `:select-actions` - Vector of actions to perform to select collection or scene.
                        Use :portfolio.ui.scene-browser/target-id as a placeholder
                        for the selected id.
  - `:path-ctx` - The state path context. Allows for multiple individual menus with
                  their own state for expand/collapse etc.
  - `:location` - Optional. When provided, `:url` will be included for scene options."
  [state opt]
  (prepare-browser-collections state opt []))
