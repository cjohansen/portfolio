(ns portfolio.data)

(def scenes (atom {}))
(def scene-order (atom 0))
(def namespaces (atom {}))
(def collections (atom {}))

(defn debounce [f ms]
  #?(:cljs
     (let [timer (atom nil)]
       (fn [& args]
         (some-> @timer js/clearTimeout)
         (reset! timer (js/setTimeout #(apply f args) ms))))
     :clj (fn [& args] (apply f args))))

(defn get-deleted-scenes
  "Finds scenes that have been deleted. All the scenes in the same namespace
  should have :idx 1 apart. If there are scenes in a namespace that are
  separated from the rest with :idx more than 1 apart, it means the other scenes
  have been re-defined. "
  [scenes]
  (->> (vals scenes)
       (group-by (comp namespace :id))
       (mapcat
        (fn [[_ scenes]]
          (->> scenes
               (sort-by :idx)
               reverse
               (partition-all 2 1)
               (drop-while (fn [[a b]] (= (- (:idx a) (:idx b)) 1)))
               (keep second))))))

(defn purge-removed-scenes []
  (swap! scenes
         (fn [scenes]
           (apply dissoc scenes (map :id (get-deleted-scenes scenes))))))

(def eventually-purge-scenes (debounce purge-removed-scenes 50))

(defn get-scene-context
  "Finds the line number (if available) and index of the scene. Line number 1 very
  likely means the form was sent to the REPL, not recompiled from a file. When
  that is the case, we reuse the existing index and line number if possible."
  [old new]
  (if (= 1 (:line new))
    {:line (:line old)
     :idx (or (:idx old)
              (swap! scene-order inc))}
    {:line (or (:line new) (:line old))
     :idx (swap! scene-order inc)}))

(defn register-scene! [scene]
  (if-not (:id scene)
    (throw (ex-info "Cannot register scene without :id" {:scene scene}))
    (let [{:keys [idx line]} (get-scene-context (get-in @scenes [(:id scene)]) scene)]
      (swap! scenes assoc (:id scene)
             (cond-> (assoc scene
                            :line line
                            :idx idx ;;(.indexOf @scene-order (:id scene))
                            :updated-at #?(:cljs (.getTime (js/Date.))
                                           :clj (.toEpochMilli (java.time.Instant/now))))
               (empty? (:title scene))
               (assoc :title (name (:id scene)))))
      (eventually-purge-scenes)
      nil)))

(defn register-namespace! [ns]
  (if-not (:namespace ns)
    (throw (ex-info "Cannot register namespace without :namespace" {:namespace ns}))
    (swap! namespaces assoc (:namespace ns) ns)))

(defn register-collection! [collection]
  (if-not (:id collection)
    (throw (ex-info "Cannot register collection without :id" {:id collection}))
    (swap! collections assoc (:id collection) collection)))
