(ns portfolio.core
  (:require [portfolio.protocols :as portfolio]
            [portfolio.router :as router]))

(defn get-current-scene [state location]
  (or (when-let [scene (some-> location :query-params :scene keyword)]
        (->> (:scenes state)
             (filter (comp #{scene} :id))
             first))
      (first (:scenes state))))

(defn get-current-scenes [state location]
  (or (when-let [scene (some-> location :query-params :scene keyword)]
        (->> (:scenes state)
             (filter (comp #{scene} :id))
             (take 1)))
      (when-let [ns (some-> location :query-params :namespace keyword)]
        (->> (:scenes state)
             (filter (comp keyword? :id))
             (filter (comp #{ns} namespace :id))))
      (take 1 (:scenes state))))

(defn get-scene-namespace [{:keys [namespaces]} {:keys [id]}]
  (->> namespaces
       (filter (comp #{(some-> id namespace)} :namespace))
       first))

(defn get-scene-collection [state scene]
  (->> (get-scene-namespace state scene)
       :collection))

(defn get-collection [state collection]
  (->> (:collections state)
       (filter (comp #{collection} :id))
       first))

(defn get-current-view [state location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn prepare-scene-link [current location {:keys [id title]}]
  (let [selected? (= id (:id current))]
    (cond-> {:title title}
      (not selected?) (assoc :url (router/get-url (assoc-in location [:query-params :scene] id)))
      selected? (assoc :selected? true))))

(defn namespace-selected? [state ns scenes]
  (contains? (set (map :id scenes)) (:id (:current-scene state))))

(defn namespace-expanded? [state ns scenes]
  (or (get-in state [ns :expanded?])
      (namespace-selected? state ns scenes)))

(defn prepare-scenes [state location scenes]
  (->> scenes
       (group-by (comp namespace :id))
       (map (fn [[ns scenes]]
              (let [expanded? (namespace-expanded? state ns scenes)
                    selected? (namespace-selected? state ns scenes)]
                (cond->
                    {:title (->> (:namespaces state)
                                 (filter (comp #{ns} :namespace))
                                 first
                                 :title)}
                  (not selected?)
                  (assoc :actions [[:assoc-in [ns :expanded?] (not expanded?)]])

                  expanded?
                  (into {:expanded? true
                         :selected? selected?
                         :items (map #(prepare-scene-link (:current-scene state) location %) scenes)})))))))

(defn prepare-sidebar [state location]
  {:width 230
   :title (or (not-empty (:title state)) "Portfolio")
   :lists (->> (:scenes state)
               (group-by #(get-scene-collection state %))
               (sort-by first)
               (map (fn [[collection scenes]]
                      {:title (or (:title (get-collection state collection))
                                  (some-> collection name))
                       :items (prepare-scenes state location scenes)})))})

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn realize-scene [scene]
  (cond-> scene
    (:component-fn scene) (assoc :component ((:component-fn scene) (:args scene)))))

(defn prepare-data [state location]
  (let [current-scene (realize-scene (get-current-scene state location))
        current-namespace (get-scene-namespace state current-scene)
        current-view (get-current-view state location)
        state (assoc state
                     :current-scene current-scene
                     :current-namespace current-namespace
                     :current-collection (get-collection state (:collection current-namespace)))]
    {:sidebar (prepare-sidebar state location)
     :toolbar (map #(prepare-view-option current-view %) (:views state))
     :view (portfolio/prepare-data current-view state location)}))
