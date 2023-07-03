(ns portfolio.data
  (:require [phosphor.icons :as icons]
            [portfolio.homeless :as h]))

(def scenes (atom {}))
(def scene-order (atom 0))
(def collections (atom {}))
(def render-fns (atom []))

(defn get-deleted-scenes
  "Finds scenes that have been deleted. All the scenes in the same namespace
  should have :idx 1 apart. If there are scenes in a namespace that are
  separated from the rest with :idx more than 1 apart, it means the other scenes
  have been re-defined."
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

(def eventually-purge-scenes (h/debounce purge-removed-scenes 50))

(defn get-scene-context
  "Finds the line number (if available) and index of the scene. Line number 1 very
  likely means the form was sent to the REPL, not recompiled from a file. When
  that is the case, we reuse the existing index and line number if possible."
  [old new]
  (cond
    (:idx new)
    {:line (or (:line new) (:line old))
     :idx (:idx new)}

    (= 1 (:line new))
    {:line (:line old)
     :idx (or (:idx old)
              (swap! scene-order inc))}

    (nil? (:idx new))
    {:line (or (:line new) (:line old))
     :idx (swap! scene-order inc)}))

(defn get-next-idx [ns]
  (->> (vals @scenes)
       (filter (comp #{ns} namespace :id))
       (map :idx)
       (apply max -1)
       inc))

(defn register-scene! [scene]
  (if-not (:id scene)
    (throw (ex-info "Cannot register scene without :id" {:scene scene}))
    (let [{:keys [idx line]} (get-scene-context (get-in @scenes [(:id scene)]) scene)]
      (swap! scenes assoc (:id scene)
             (cond-> (assoc (dissoc scene :param)
                            :line line
                            :idx idx
                            :updated-at #?(:cljs (.getTime (js/Date.))
                                           :clj (.toEpochMilli (java.time.Instant/now))))
               (empty? (:title scene))
               (assoc :title (h/->title (name (:id scene))))

               (nil? (:collection scene))
               (assoc :collection (some-> scene :id namespace keyword))

               (:param scene)
               (assoc :params (:param scene))))
      (eventually-purge-scenes)
      nil)))

(defn register-collection! [id collection]
  (assert (keyword? id) "register-collection! must be called with a keyword id as first argument")
  (swap! collections assoc id (assoc collection :id id)))

(defn create-repl-scene [scene]
  (let [idx (get-next-idx "portfolio.repl")]
    (merge scene
           {:id (keyword "portfolio.repl" (str "scene-" #?(:cljs (.getTime (js/Date.)))))
            :title (or (:title scene) #?(:cljs (re-find #"\d\d:\d\d:\d\d" (str (js/Date.)))))
            :idx idx
            :line (- idx)
            :icon (icons/icon :phosphor.regular/terminal)
            :collection :portfolio.repl})))

(defn register-repl-scene! [scene]
  (register-scene! scene)
  (when-not (:portfolio.repl @collections)
    (register-collection!
     :portfolio.repl
     {:kind :folder
      :collection nil
      :icon (icons/icon :phosphor.regular/terminal-window)
      :title "REPL"})))

(defn register-scene-renderer! [f]
  (swap! render-fns conj f))

(defn get-tapped-scene [x]
  (when x
    (loop [fs @render-fns]
      (let [f (first fs)
            scene (when f (f x))]
        (cond
          scene (create-repl-scene scene)
          (next fs) (recur (next fs)))))))
