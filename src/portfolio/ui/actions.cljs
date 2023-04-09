(ns portfolio.ui.actions
  (:require [clojure.walk :as walk]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.css :as css]
            [portfolio.ui.layout :as layout]
            [portfolio.ui.router :as router]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.scene :as scene]
            [portfolio.ui.scene-browser :as scene-browser]
            [portfolio.ui.search.index :as index]))

(defn assoc-in*
  "Takes a map and pairs of path value to assoc-in to the map. Makes `assoc-in`
  work like `assoc`, e.g.:

  ```clj
  (assoc-in* {}
             [:person :name] \"Christian\"
             [:person :language] \"Clojure\")
  ;;=>
  {:person {:name \"Christian\"
            :language \"Clojure\"}}
  ```"
  [m & args]
  (assert (= 0 (mod (count args) 2)) "assoc-in* takes a map and pairs of path value")
  (assert (->> args (partition 2) (map first) (every? vector?)) "each path should be a vector")
  (->> (partition 2 args)
       (reduce (fn [m [path v]]
                 (assoc-in m path v)) m)))

(defn dissoc-in*
  "Takes a map and paths to dissoc from it. An example explains it best:

  ```clj
  (dissoc-in* {:person {:name \"Christian\"
                        :language \"Clojure\"}}
              [:person :language])
  ;;=>
  {:person {:name \"Christian\"}}
  ```

  Optionally pass additional paths.
  "
  [m & args]
  (reduce (fn [m path]
            (cond
              (= 0 (count path)) m
              (= 1 (count path)) (dissoc m (first path))
              :else (let [[k & ks] (reverse path)]
                      (update-in m (reverse ks) dissoc k))))
          m args))

(defn atom? [x]
  (satisfies? cljs.core/IWatchable x))

(defn get-page-title [state selection]
  (let [suffix (when (:title state) (str " - " (:title state)))]
    (if (:target selection)
      (case (:kind selection)
        :scene (str "Scene: " (:title (:target selection)) suffix)
        :collection (str "Collection: " (:title (:target selection)) suffix))
      (str "No scenes found" suffix))))

(defn go-to-location [state location]
  (let [id (routes/get-id (:location state))
        current-scenes (collection/get-selected-scenes state id)
        selection (collection/get-selection state (routes/get-id location))
        layout (layout/get-view-layout state selection)
        lp (layout/get-layout-path layout)
        expansions (->> (:path selection)
                        (map scene-browser/get-expanded-path)
                        (remove #(get-in state %))
                        (mapcat (fn [path] [path true])))]
    {:assoc-in (cond-> [[:location] location
                        (layout/get-current-layout-path) lp]
                 (nil? (get-in state lp)) (into [lp layout])
                 (seq expansions) (into expansions))
     :fns (concat
           (->> (filter :on-unmount current-scenes)
                (map (fn [{:keys [on-unmount params id title]}]
                       (into [:on-unmount (or id title) on-unmount] params))))
           (->> (filter :on-mount (:scenes selection))
                (map (fn [{:keys [on-mount params id title]}]
                       (into [:on-mount (or id title) on-mount] params)))))
     :release (mapcat scene/get-scene-atoms current-scenes)
     :subscribe (mapcat scene/get-scene-atoms (:scenes selection))
     :set-page-title (get-page-title state selection)
     :update-window-location (router/get-url location)}))

(defn remove-scene-param
  ([state scene-id]
   (let [param (get-in state [:scenes scene-id :param])]
     (cond
       (map? param)
       {:actions [[:dissoc-in [:ui scene-id :overrides]]]}

       (atom? param)
       {:reset [param (get-in state [:ui scene-id :original])]
        :actions [[:dissoc-in [:ui scene-id :overrides]]
                  [:dissoc-in [:ui scene-id :original]]]})))
  ([state scene-id k]
   (let [param (get-in state [:scenes scene-id :param])]
     (cond
       (map? param)
       {:actions [[:dissoc-in [:ui scene-id :overrides k]]]}

       (atom? param)
       {:swap [param [k] (get-in state [:scenes scene-id :original k])]
        :actions [[:dissoc-in [:ui scene-id :overrides k]]
                  [:dissoc-in [:ui scene-id :original k]]]}))))

(defn set-scene-param
  ([state scene-id v]
   (let [param (get-in state [:scenes scene-id :param])]
     (cond
       (map? param)
       {:actions [[:assoc-in [:ui scene-id :overrides] v]]}

       (atom? param)
       {:reset [param v]
        :actions [[:assoc-in [:ui scene-id :overrides] v]
                  [:assoc-in [:ui scene-id :original] @param]]})))
  ([state scene-id k v]
   (let [param (get-in state [:scenes scene-id :param])]
     (cond
       (map? param)
       {:actions [[:assoc-in [:ui scene-id :overrides k] v]]}

       (atom? param)
       {:swap [param [k] v]
        :actions (cond-> [[:assoc-in [:ui scene-id :overrides k] v]]
                   (not (get-in state [:ui scene-id :original k]))
                   (into [[:assoc-in [:ui scene-id :original k] (k @param)]]))}))))

(defn search [{:keys [index]} q]
  (when index
    {:assoc-in [[:search/suggestions] (index/search index q)]}))

(declare execute-action!)

(defn process-action-result! [app res]
  (doseq [ref (:release res)]
    (println "Stop watching atom" (pr-str ref))
    (remove-watch ref ::portfolio))
  (doseq [[k t f & args] (:fns res)]
    (println (str "Calling " k " on " t " with") (pr-str args))
    (apply f args))
  (doseq [ref (:subscribe res)]
    (println "Start watching atom" (pr-str ref))
    (add-watch ref ::portfolio
      (fn [_ _ _ _]
        (swap! app update :heartbeat (fnil inc 0)))))
  (when-let [url (:update-window-location res)]
    (when-not (= url (router/get-current-url))
      (println "Updating browser URL to" url)
      (.pushState js/history false false url)))
  (when-let [title (:set-page-title res)]
    (println (str "Set page title to '" title "'"))
    (set! js/document.title title))
  (when (or (:dissoc-in res) (:assoc-in res))
    (when (:assoc-in res)
      (println ":assoc-in" (pr-str (:assoc-in res))))
    (when (:dissoc-in res)
      (println ":dissoc-in" (pr-str (:dissoc-in res))))
    (swap! app (fn [state]
                 (apply assoc-in*
                        (apply dissoc-in* state (:dissoc-in res))
                        (:assoc-in res)))))
  (doseq [action (:actions res)]
    (execute-action! app action))
  (when-let [[ref path v] (:swap res)]
    (swap! ref assoc-in path v))
  (when-let [[ref v] (:reset res)]
    (reset! ref v))
  (when-let [paths (:load-css-files res)]
    (css/load-css-files paths))
  (when-let [paths (:replace-css-files res)]
    (css/replace-loaded-css-files paths)))

(defn execute-action! [app action]
  (println "execute-action!" action)
  (process-action-result!
   app
   (case (first action)
     :assoc-in {:assoc-in (rest action)}
     :dissoc-in {:dissoc-in (rest action)}
     :fn/call (let [[fn & args] (rest action)] (apply fn args))
     :go-to-location (apply go-to-location @app (rest action))
     :go-to-current-location (go-to-location @app (router/get-current-location))
     :set-css-files (let [[paths] (rest action)]
                      {:assoc-in [[:css-paths] paths]
                       :load-css-files paths
                       :replace-css-files paths})
     :remove-scene-param (apply remove-scene-param @app (rest action))
     :set-scene-param (apply set-scene-param @app (rest action))
     :search (apply search @app (rest action))))
  app)

(def available-actions
  #{:assoc-in :dissoc-in :go-to-location :go-to-current-location
    :remove-scene-param :set-scene-param :fn/call :event/prevent-default
    :search})

(defn actions? [x]
  (and (sequential? x)
       (not (empty? x))
       (every? #(and (sequential? %)
                     (contains? available-actions (first %))) x)))

(defn parse-int [s]
  (let [n (js/parseInt s 10)]
    (if (not= n n)
      ;; NaN!
      0
      n)))

(defn actionize-data
  "Given a Portfolio `app` instance and some prepared data to render, wrap
  collections of actions in a function that executes these actions. Using this
  function makes it possible to prepare event handlers as a sequence of action
  tuples, and have them seemlessly emitted as actions in the components.

  If you need to access the `.-value` of the event target (e.g. for on-change on
  input fields, etc), use `:event.target/value` as a placeholder in your action,
  and it will be replaced with the value."
  [app data]
  (walk/prewalk
   (fn [x]
     (if (actions? x)
       (fn [e]
         (when (->> (tree-seq coll? identity x)
                    (filter #{[:event/prevent-default]})
                    seq)
           (.preventDefault e)
           (.stopPropagation e))
         (doseq [action (remove #{[:event/prevent-default]} x)]
           (execute-action!
            app
            (walk/prewalk
             (fn [ax]
               (cond
                 (= :event.target/value ax)
                 (some-> e .-target .-value)

                 (= :event.target/number-value ax)
                 (some-> e .-target .-value parse-int)

                 :else ax))
             action))))
       x))
   data))
