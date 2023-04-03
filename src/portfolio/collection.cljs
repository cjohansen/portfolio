(ns portfolio.collection
  (:require [clojure.string :as str]))

(defn by-parent-id [parent-id]
  #(= parent-id (:collection %)))

(defn ns->path [ns]
  (str/split ns #"\."))

(defn get-paths
  "Returns a list of unique paths represented by the namespaces. Discards
  namespace prefixes shared by all namespaces, and turns the remaining part
  of the namespace into a list:

  ```clj
  (get-paths [\"ui.components.button\"
              \"ui.components.matrix\"
              \"ui.components.pill\"])
  ;;=> ((\"button\") (\"matrix\") (\"pill\"))
  ```

  ```clj
  (get-paths [\"ui.components.button\"
              \"ui.components.pill\"
              \"ui.icons\"])
  ;;=> ((\"components\" \"button\") (\"components\" \"pill\") (\"icons\"))
  "
  [namespaces]
  (let [paths (map ns->path namespaces)]
    (loop [candidates (drop-last 1 (first paths))
           paths paths
           prefix []]
      (if (and (seq candidates)
               (every? (comp #{(first candidates)} first) paths)
               (every? #(< 2 (count %)) paths))
        (recur (next candidates) (map #(drop 1 %) paths) (conj prefix (ffirst paths)))
        (cond-> {:paths paths}
          (seq prefix) (assoc :prefix (str/join "." prefix)))))))

(defn get-collection-title [s]
  (some-> (name s)
          (str/replace #"-" " ")
          str/capitalize))

(defn make-collection [prefix path]
  {:id (keyword (str prefix "." (str/join "." path)))
   :title (get-collection-title (last path))})

(defn suggest-collections [scenes]
  (let [{:keys [paths prefix]} (get-paths (map (comp namespace :id) scenes))]
    (->> paths
         (group-by first)
         (mapcat
          (fn [[coll xs]]
            (let [parent (-> (make-collection prefix [coll])
                             (assoc :kind :folder))]
              (conj
               (for [coll xs]
                 (-> (make-collection prefix coll)
                     (assoc :collection (:id parent))
                     (assoc :kind :package)))
               parent)))))))

(defn merge-collections [collections defaults]
  (->> (for [k (concat (keys defaults) (keys collections))]
         [k (merge (get defaults k) (get collections k))])
       (into {})))

(defn ensure-defaults [collection scenes]
  (cond-> collection
    (empty? (:title collection))
    (assoc :title (get-collection-title (last (str/split (name (:id collection)) #"\."))))

    (nil? (:kind collection))
    (assoc :kind (if (some (comp #{(:id collection)} :collection) scenes)
                   :package
                   :folder))))

(defn get-default-organization [scenes collections]
  (let [collections (merge
                     (->> (keep :collection scenes)
                          (map (fn [id]
                                 [id {:id id}]))
                          (into {}))
                     (->> collections
                          (map (juxt :id identity))
                          (into {})))
        defaults (->> scenes
                      suggest-collections
                      (map (juxt :id identity))
                      (into {}))]
    (->> (for [k (concat (keys defaults) (keys collections))]
           [k (-> (merge (get defaults k) (get collections k))
                  (ensure-defaults scenes))])
         (into {}))))

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

(defn get-active-scenes [{:keys [scenes] :as state} id]
  (or (seq (filter (comp #{id} :id) (vals scenes)))
      (get-collection-scenes state [id])))

(defn get-selection [state id]
  (let [scene (first (filter (comp #{id} :id) (vals (:scenes state))))]
    {:scenes (get-active-scenes state id)
     :kind (if scene :scene :collection)
     :path (get-collection-path state id)
     :target (or scene
                 (first (filter (comp #{id} :id) (:collections state))))}))

(defn get-sort-key [collection]
  [(:idx collection 999999999)
   (or (some-> collection :title str/lower-case)
       (some-> collection :id name))])
