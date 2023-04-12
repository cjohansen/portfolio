(ns portfolio.ui.collection
  (:require [clojure.string :as str]
            [portfolio.homeless :as h]
            [portfolio.ui.routes :as routes]))

(defn by-parent-id [parent-id]
  #(= parent-id (:collection %)))

(defn ns->path [ns]
  (str/split ns #"\."))

(defn path->id [path]
  (keyword (str/join "." path)))

(defn get-collection-title [s]
  (some-> (if (keyword? s)
            (name s)
            s)
          (str/split #"\.")
          last
          h/title-case))

(defn suggest-packages [scenes]
  (->> scenes
       (map #(or (some-> % :collection name) (namespace (:id %))))
       (map ns->path)
       (map (fn [path]
              (let [id (path->id path)]
                (cond-> {:id id
                         :title (get-collection-title (last path))
                         :kind :package}
                  (< 1 (count path))
                  (assoc :collection (path->id (drop-last 1 path)))))))))

(defn ensure-defaults [collection scenes]
  (cond-> collection
    (empty? (:title collection))
    (assoc :title (get-collection-title (:id collection)))

    (nil? (:kind collection))
    (assoc :kind (if (some (comp #{(:id collection)} :collection) scenes)
                   :package
                   :folder))))

(defn get-default-organization [scenes collections]
  (let [existing (into {} (map (juxt :id identity) collections))
        packages (suggest-packages scenes)
        folders (->> (keep :collection packages)
                     set
                     (map (fn [id]
                            {:id id
                             :title (get-collection-title id)
                             :kind :folder})))
        configured-folders (->> packages
                                (keep :collection)
                                (filter existing))
        folder-n (count (set (concat (map :id folders) configured-folders)))]
    (->> (if (or (< 1 folder-n) (seq configured-folders))
           (concat packages folders)
           (map #(dissoc % :collection) packages))
         (map #(merge % (get existing (:id %))))
         (map #(ensure-defaults % scenes))
         set)))

(defn get-collection-path [{:keys [scenes collections]} id]
  (let [target (or (first (filter (comp #{id} :id) (vals scenes)))
                   (first (filter (comp #{id} :id) (vals collections))))]
    (loop [res (if target (list target) (list))]
      (let [parent-id (:collection (first res))]
        (if (and parent-id (not (some (comp #{parent-id} :id) res)))
          (recur (->> (vals collections)
                      (filter (comp #{parent-id} :id))
                      first
                      (conj res)))
          res)))))

(defn get-collection-scenes [{:keys [collections scenes]} ids]
  (loop [res []
         ids (set ids)]
    (if (seq ids)
      (recur
       (->> (vals scenes)
            (filter (comp ids :collection))
            (concat res))
       (->> (vals collections)
            (filter (comp ids :collection))
            (map :id)
            set))
      res)))

(defn get-selected-scenes [{:keys [scenes] :as state} id]
  (or (seq (filter (comp #{id} :id) (vals scenes)))
      (get-collection-scenes state [id])))

(defn get-selection [state id]
  (let [scene (first (filter (comp #{id} :id) (vals (:scenes state))))
        target (or scene
                   (->> (vals (:collections state))
                        (filter (comp #{id} :id))
                        first))]
    (when target
      {:scenes (get-selected-scenes state id)
       :kind (if scene :scene :collection)
       :path (get-collection-path state id)
       :target target})))

(defn by-id [state id]
  (or (->> (vals (:scenes state))
           (filter (comp #{id} :id))
           first)
      (->> (vals (:collections state))
           (filter (comp #{id} :id))
           first)))

(defn get-sort-key [collection]
  [(:idx collection 999999999)
   (or (some-> collection :title str/lower-case)
       (some-> collection :id name))])

(defn get-in-parents
  "Look for key `k` in map `m`. If not present, traverse collection hierarchy via
  `:collection` and look in the parent collection. If the key is not found in
  any parent, finally try the `state`."
  [state m k]
  (loop [m m]
    (or (get m k)
        (if-let [id (:collection m)]
          (recur (get-in state [:collections id]))
          (get state k)))))

(defn get-folder-illustration [state collection expanded?]
  {:icon (or (if expanded?
               (:expanded-icon collection)
               (:collapsed-icon collection))
             (:icon collection)
             (if expanded?
               (get-in-parents state collection :default-folder-expanded-icon)
               (get-in-parents state collection :default-folder-collapsed-icon))
             (get-in-parents state collection :default-folder-icon)
             (if expanded?
               :portfolio.ui.icons/folder-open
               :portfolio.ui.icons/folder))
   :color (or (if expanded?
                (:expanded-icon-color collection)
                (:collapsed-icon-color collection))
              (:icon-color collection)
              "var(--folder-icon-color)")})

(defn get-package-illustration [state collection expanded?]
  {:icon (or (if expanded?
               (:expanded-icon collection)
               (:collapsed-icon collection))
             (:icon collection)
             (if expanded?
               (get-in-parents state collection :default-package-expanded-icon)
               (get-in-parents state collection :default-package-collapsed-icon))
             (get-in-parents state collection :default-package-icon)
             :portfolio.ui.icons/cube)
   :color (or (if expanded?
                (:expanded-icon-color collection)
                (:collapsed-icon-color collection))
              (:icon-color collection)
              "var(--highlight-color)")})

(defn get-scene-illustration [state scene selected?]
  {:icon (or (when selected?
               (:selected-icon scene))
             (:icon scene)
             (when selected?
               (get-in-parents state scene :default-scene-selected-icon))
             (get-in-parents state scene :default-scene-icon)
             :portfolio.ui.icons/bookmark)
   :color (or (when selected?
                (:selected-icon-color scene))
              (:icon-color scene)
              (when-not selected?
                "var(--browser-unit-icon-color)"))})

(defn get-illustration [item state & [current?]]
  (case (:kind item)
    :folder (get-folder-illustration state item current?)
    :package (get-package-illustration state item current?)
    (get-scene-illustration state item current?)))

(defn prepare-selection-menu-bar [state selection {:keys [expand-path location tight?]}]
  {:title (if tight?
            [{:text (:title (:target selection))}]
            (for [item (:path selection)]
              (cond-> {:text (:title item)}
                (and (not= (:target selection) item) location)
                (assoc :url (routes/get-url location item)))))

   :action {:icon (if (get-in state expand-path)
                    :portfolio.ui.icons/caret-up
                    :portfolio.ui.icons/caret-down)
            :actions [[:assoc-in expand-path (not (get-in state expand-path))]]}
   :illustration (some-> (:target selection) (get-illustration state))})
