(ns portfolio.core
  (:require [clojure.string :as str]
            [portfolio.code :as code]
            [portfolio.router :as router]
            [portfolio.view :as view]))

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
  (let [ns (some-> id namespace)]
    (or (get namespaces ns)
        (when ns
          {:title ns
           :namespace ns}))))

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
                         :items (->> scenes
                                     (sort-by (juxt :line :idx))
                                     (map #(prepare-scene-link location %)))})))))))

(defn small-screen? [state]
  (< (-> state :win :w) 650))

(defn sidebar? [{:keys [sidebar-status] :as state}]
  (cond
    (= sidebar-status :hidden)
    false

    (= sidebar-status :visible)
    true

    :default
    (not (small-screen? state))))

(defn prepare-header [state _]
  (when-not (sidebar? state)
    {:title (or (not-empty (:title state)) "Portfolio")
     :actions [[:assoc-in [:sidebar-status]
                (if (small-screen? state)
                  :visible
                  :auto)]]}))

(defn prepare-sidebar [state location]
  (when (sidebar? state)
    {:width 250
     :slide? (boolean (:sidebar-status state))
     :title (or (not-empty (:title state)) "Portfolio")
     :actions [[:assoc-in [:sidebar-status]
                (if (small-screen? state)
                  :auto
                  :hidden)]]
     :lists (->> (:scenes state)
                 vals
                 (group-by #(get-scene-collection state %))
                 (sort-by first)
                 (map (fn [[collection scenes]]
                        {:title (or (:title (get-collection state collection))
                                    (when-not (= ::default collection)
                                      (some-> collection name)))
                         :items (prepare-scenes state location (sort-by :title scenes))})))}))

(defn prepare-view-option [current-view view]
  (cond-> view
    (= (:id current-view) (:id view))
    (assoc :selected? true)))

(defn get-scene-param-overrides [state scene]
  (get-in state [:ui (:id scene) :overrides]))

(defn get-scene-param [state scene]
  (if (map? (:param scene))
    (merge (:param scene) (get-scene-param-overrides state scene))
    (:param scene)))

(defn prep-scene-fns [state scenes]
  (for [scene scenes]
    (let [param (get-scene-param state scene)]
      (cond-> (assoc scene :component-param (code/code-str param))
        (:component scene)
        (assoc :component-fn #(:component scene))

        (:component-fn scene)
        (assoc :component-fn #((:component-fn scene) param %))))))

(defn prepare-data [state location]
  (let [current-scenes (->> (get-current-scenes state location)
                            (prep-scene-fns state)
                            (sort-by :idx))
        ;; There might be multiple scenes, but multiple scenes across different
        ;; namespaces is not (yet) supported.
        current-namespace (get-scene-namespace state (first current-scenes))
        current-view (get-current-view state location)
        state (assoc state
                     :current-scenes current-scenes
                     :current-namespace current-namespace
                     :current-collection (get-collection state (:collection current-namespace)))]
    {:header (prepare-header state location)
     :sidebar (prepare-sidebar state location)
     :small? (small-screen? state)
     :tab-bar {:tabs (map #(prepare-view-option current-view %) (:views state))}
     :view (view/prepare-data current-view state location)}))

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
           paths paths]
      (if (and (not (empty? candidates))
               (every? (comp #{(first candidates)} first) paths))
        (recur (next candidates) (map #(drop 1 %) paths))
        paths))))

(defn get-collection-title [s]
  (some-> s
          (str/replace #"-" " ")
          str/capitalize))

(defn get-default-organization [namespaces collections scenes]
  (let [nses (set (map (comp namespace :id) (vals scenes)))
        paths (get-paths nses)
        colls (->> paths
                   (filter #(< 1 (count %)))
                   (map (fn [path]
                          (let [id (keyword (first path))]
                            (merge
                             {:id id
                              :title (get-collection-title (first path))}
                             (collections id)))))
                   (concat (->> (vals namespaces)
                                (keep :collection)
                                (map (fn [coll]
                                       (merge
                                        {:id coll
                                         :title (get-collection-title (name coll))}
                                        (collections coll)))))))]
    {:namespaces (into
                  namespaces
                  (->> paths
                       (map (fn [ns path]
                              [ns (merge
                                   (if colls
                                     {:namespace ns
                                      :title (str/join " / " (drop 1 path))
                                      :collection (keyword (first path))}
                                     {:namespace ns
                                      :title (str/join " / " path)})
                                   (get namespaces ns))])
                            nses)
                       (into {})))
     :collections (some->> colls
                           (map (juxt :id identity))
                           (into {}))}))

(defn init-state [config]
  (let [app (-> config
                (update :scenes #(->> % (map (juxt :id identity)) (into {})))
                (update :namespaces #(->> % (map (juxt :namespace identity)) (into {})))
                (update :collections #(->> % (map (juxt :id identity)) (into {}))))
        defaults (get-default-organization (:namespaces app) (:collections app) (:scenes app))]
    (cond-> app
      (:namespaces defaults) (assoc :namespaces (:namespaces defaults))
      (:collections defaults) (assoc :collections (:collections defaults)))))
