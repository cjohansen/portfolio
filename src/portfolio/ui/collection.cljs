(ns portfolio.ui.collection
  (:require [clojure.string :as str]))

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
          (str/replace #"-" " ")
          str/capitalize))

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
  (let [scene (first (filter (comp #{id} :id) (vals (:scenes state))))]
    {:scenes (get-selected-scenes state id)
     :kind (if scene :scene :collection)
     :path (get-collection-path state id)
     :target (or scene
                 (->> (vals (:collections state))
                      (filter (comp #{id} :id))
                      first))}))

(defn get-sort-key [collection]
  [(:idx collection 999999999)
   (or (some-> collection :title str/lower-case)
       (some-> collection :id name))])
