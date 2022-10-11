(ns portfolio.actions
  (:require [clojure.walk :as walk]
            [portfolio.core :as portfolio]
            [portfolio.router :as router]))

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

(defn go-to-location [state location]
  (let [current-scenes (portfolio/get-current-scenes state (:location state))
        next-scenes (portfolio/get-current-scenes state location)]
    {:assoc-in [[:location] location]
     :fns (into (->> (filter :on-unmount current-scenes)
                     (map (fn [{:keys [on-unmount args id title]}]
                            [:on-unmount (or id title) on-unmount args])))
                (->> (filter :on-mount next-scenes)
                     (map (fn [{:keys [on-mount args id title]}]
                            [:on-mount (or id title) on-mount args]))))
     :release (->> (map :args current-scenes)
                   (filter atom?)
                   (map (fn [ref] [ref ::portfolio])))
     :subscribe (->> (map :args next-scenes)
                     (filter atom?)
                     (map (fn [ref] [ref ::portfolio])))
     :update-window-location (router/get-url location)}))

(defn remove-scene-argument [state scene-id k]
  (let [args (get-in state [:scenes scene-id :args])]
    (cond
      (map? args)
      {:actions [[:dissoc-in [:ui scene-id :overrides k]]]}

      (atom? args)
      {:swap [args [k] (get-in state [:scenes scene-id :original k])]
       :actions [[:dissoc-in [:ui scene-id :overrides k]]
                 [:dissoc-in [:ui scene-id :original k]]]})))

(defn set-scene-argument [state scene-id k v]
  (let [args (get-in state [:scenes scene-id :args])]
    (cond
      (map? args)
      {:actions [[:assoc-in [:ui scene-id :overrides k] v]]}

      (atom? args)
      {:swap [args [k] v]
       :actions (cond-> [[:assoc-in [:ui scene-id :overrides k] v]]
                  (not (get-in state [:ui scene-id :original k]))
                  (into [[:assoc-in [:ui scene-id :original k] (k @args)]]))})))

(declare execute-action!)

(defn process-action-result! [app res]
  (doseq [[ref k] (:release res)]
    (println "Stop watching atom" (pr-str ref))
    (remove-watch ref k))
  (doseq [[k t f & args] (:fns res)]
    (println (str "Calling " k " on " t " with") (pr-str args))
    (apply f args))
  (doseq [[ref k] (:subscribe res)]
    (println "Start watching atom" (pr-str ref))
    (add-watch ref k (fn [_ _ _ _] (swap! app update :heartbeat (fnil inc 0)))))
  (when-let [url (:update-window-location res)]
    (when-not (= url (router/get-current-url))
      (println "Updating browser URL to" url)
      (.pushState js/history false false url)))
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
    (swap! ref assoc-in path v)))

(defn execute-action! [app action]
  (println "execute-action!" action)
  (process-action-result!
   app
   (case (first action)
     :assoc-in {:assoc-in (rest action)}
     :dissoc-in {:dissoc-in (rest action)}
     :go-to-location (apply go-to-location @app (rest action))
     :go-to-current-location (go-to-location @app (router/get-current-location))
     :remove-scene-argument (apply remove-scene-argument @app (rest action))
     :set-scene-argument (apply set-scene-argument @app (rest action))))
  app)

(def available-actions
  #{:assoc-in :dissoc-in :go-to-location :go-to-current-location
    :remove-scene-argument :set-scene-argument})

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
         (doseq [action x]
           (execute-action!
            app
            (walk/prewalk
             (fn [ax]
               (cond
                 (= :event.target/value ax)
                 (some-> e .-target .-value)

                 (= :event.target/number-value ax)
                 (some-> e .-target .-value parse-int)

                 :default ax))
             action))))
       x))
   data))
