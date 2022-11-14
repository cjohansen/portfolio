(ns portfolio.core
  (:require [cljs.pprint :as pprint]
            [portfolio.router :as router]
            [portfolio.view :as view]))

(defn blank? [x]
  (or (nil? x)
      (and (coll? x) (empty? x))
      (and (string? x) (empty? x))))

(defn code-str [data]
  (when (not (blank? data))
    (with-out-str (pprint/pprint data))))

(defn get-current-scenes [state location]
  (or (when-let [scene (some-> location :query-params :scene keyword)]
        (->> (:scenes state)
             vals
             (filter (comp #{scene} :id))
             (take 1)))
      (when-let [ns (some-> location :query-params :namespace)]
        (->> (:scenes state)
             vals
             (filter (comp keyword? :id))
             (filter (comp #{ns} namespace :id))))))

(defn get-scene-namespace [{:keys [namespaces]} {:keys [id]}]
  (or (get namespaces (some-> id namespace))
      (when-let [ns (some-> id namespace)]
        {:title ns
         :namespace ns})))

(defn get-scene-collection [state scene]
  (let [ns (get-scene-namespace state scene)]
    (or (:collection ns) ::default)))

(defn get-collection [state collection]
  (or (get-in state [:collections collection])
      {:id collection}))

(defn get-current-view [state location]
  ;; TODO: Eventually support more views
  (first (:views state)))

(defn get-scene-url [location scene]
  (router/get-url (assoc location :query-params {:scene (:id scene)})))

(defn prepare-scene-link [location {:keys [id title] :as scene}]
  (let [selected? (= id (some-> (get-in location [:query-params :scene]) keyword))]
    (cond-> {:title title}
      (not selected?) (assoc :url (get-scene-url location scene))
      selected? (assoc :selected? true))))

(defn namespace-selected? [state ns scenes]
  (->> (:current-scenes state)
       (filter #(contains? (set (map :id scenes)) (:id %)))
       seq))

(defn namespace-expanded? [state ns scenes]
  (get-in state [:ui ns :expanded?]))

(defn prepare-scenes [state location scenes]
  (->> scenes
       (group-by (comp namespace :id))
       (sort-by first)
       (map (fn [[ns scenes]]
              (let [expanded? (namespace-expanded? state ns scenes)
                    selected? (namespace-selected? state ns scenes)
                    current-ns (get-in location [:query-params :namespace])
                    browsing? (= ns current-ns)]
                (cond->
                    {:title (:title (get-scene-namespace state (first scenes)))
                     :expand-actions [[:assoc-in [:ui ns :expanded?] (not expanded?)]]
                     :selected? selected?}

                  (not browsing?)
                  (assoc :actions
                         (cond-> [[:go-to-location (assoc location :query-params {:namespace ns})]
                                  [:assoc-in [:ui ns :expanded?] true]]
                           current-ns
                           (into [[:assoc-in [:ui current-ns :expanded?] false]])))

                  (or expanded? (and selected? (not browsing?)))
                  (into {:expanded? true
                         :items (map #(prepare-scene-link location %) scenes)})))))))

(defn prepare-sidebar [state location]
  {:width 250
   :title (or (not-empty (:title state)) "Portfolio")
   :lists (->> (:scenes state)
               vals
               (group-by #(get-scene-collection state %))
               (sort-by first)
               (map (fn [[collection scenes]]
                      {:title (or (:title (get-collection state collection))
                                  (when-not (= ::default collection)
                                    (some-> collection name)))
                       :items (prepare-scenes state location (sort-by :title scenes))})))})

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn get-scene-arg-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-scene-args [state scene]
  (if (map? (:args scene))
    (merge (:args scene) (get-scene-arg-overrides state scene))
    (:args scene)))

(defn realize-scenes [state scenes]
  (for [scene scenes]
    (let [args (get-scene-args state scene)]
      (try
        (cond-> scene
          (:component-fn scene)
          (assoc :component ((:component-fn scene) args)))
        (catch :default e
          (assoc scene
                 :error {:message (.-message e)
                         :ex-data (code-str (ex-data e))
                         :stack (.-stack e)}
                 :component-args (code-str args)))))))

(defn prepare-data [state location]
  (let [current-scenes (realize-scenes state (get-current-scenes state location))
        ;; There might be multiple scenes, but multiple scenes across different
        ;; namespaces is not (yet) supported.
        current-namespace (get-scene-namespace state (first current-scenes))
        current-view (get-current-view state location)
        state (assoc state
                     :current-scenes current-scenes
                     :current-namespace current-namespace
                     :current-collection (get-collection state (:collection current-namespace)))]
    {:sidebar (prepare-sidebar state location)
     :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
     :view (view/prepare-data current-view state location)}))

(defn init-state [config]
  (-> config
      (update :scenes #(->> % (map (juxt :id identity)) (into {})))
      (update :namespaces #(->> % (map (juxt :namespace identity)) (into {})))
      (update :collections #(->> % (map (juxt :id identity)) (into {})))))
