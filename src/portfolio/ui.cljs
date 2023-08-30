(ns portfolio.ui
  (:require [portfolio.data :as data]
            [portfolio.homeless :as h]
            [portfolio.ui.actions :as actions]
            [portfolio.ui.canvas :as canvas]
            [portfolio.ui.canvas.background :as canvas-bg]
            [portfolio.ui.canvas.code :as code]
            [portfolio.ui.canvas.compare :as compare]
            [portfolio.ui.canvas.docs :as docs]
            [portfolio.ui.canvas.grid :as canvas-grid]
            [portfolio.ui.canvas.split :as split]
            [portfolio.ui.canvas.viewport :as canvas-vp]
            [portfolio.ui.canvas.zoom :as canvas-zoom]
            [portfolio.ui.client :as client]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.search :as search]
            [portfolio.ui.search.protocols :as index]))

(defonce app (atom nil))

(defn get-collections [scenes collections]
  (->> (collection/get-default-organization (vals scenes) (vals collections))
       (map (juxt :id identity))
       (into {})))

(defn portfolio-docs? [user-v]
  (if (nil? user-v)
    (boolean (or (= "localhost" js/location.hostname)
                 (re-find #"\d+\.\d+\.\d+\.\d+" js/location.href)))
    user-v))

(defn create-app [config canvas-tools extra-canvas-tools]
  (-> config
      (update :portfolio-docs? portfolio-docs?)
      (assoc :scenes @data/scenes)
      (assoc :collections (get-collections @data/scenes @data/collections))
      (assoc :views [(canvas/create-canvas
                      {:canvas/layout (:canvas/layout config)
                       :tools (into (or canvas-tools
                                        [(canvas-bg/create-background-tool config)
                                         (canvas-vp/create-viewport-tool config)
                                         (canvas-grid/create-grid-tool config)
                                         (canvas-zoom/create-zoom-tool config)
                                         (split/create-split-tool config)
                                         (docs/create-docs-tool config)
                                         (code/create-code-tool config)
                                         (compare/create-compare-tool config)
                                         (split/create-close-tool config)])
                                    extra-canvas-tools)})])))

(def eventually-execute (h/debounce actions/execute-action! 250))

(defn index-content [app & [{:keys [ids]}]]
  (let [{:keys [index scenes collections log?]} @app]
    (when index
      (js/requestAnimationFrame
       (fn [_]
         (doseq [doc (cond->> (concat (vals scenes) (vals collections))
                       ids (filter (comp (set ids) :id)))]
           (when log?
             (println "Index" (:id doc)))
           (index/index index doc)))))))

(defn render-scene [x]
  (when-let [scene (data/get-tapped-scene x)]
    (data/register-repl-scene! scene)
    (actions/execute-action! app [:go-to-location (routes/get-scene-location (routes/get-current-location) scene)])))

(defn start! [& [{:keys [on-render config canvas-tools extra-canvas-tools index get-indexable-data] :as opt}]]
  (let [->diffable (partial search/get-diffables (or get-indexable-data search/get-indexable-data))]
    (swap! app merge (create-app config canvas-tools extra-canvas-tools) {:index index})

    (when-not (client/started? app)
      (add-watch data/scenes ::app
        (fn [_ _ old-scenes scenes]
          (let [collections (get-collections scenes (:collections @app))
                old-collections (get-collections old-scenes (:collections @app))]
            (swap! app (fn [state]
                         (-> state
                             (assoc :scenes scenes)
                             (assoc :collections collections))))
            (when (:reindex? opt true)
              (index-content
               app
               {:ids (concat
                      (search/get-diff-keys (->diffable scenes) (->diffable old-scenes))
                      (search/get-diff-keys (->diffable collections) (->diffable old-collections)))})))
          (eventually-execute app [:go-to-current-location])))

      (add-watch data/collections ::app
        (fn [_ _ _ collections]
          (let [old-collections (:collections @app)
                collections (get-collections (:scenes @app) collections)]
            (swap! app assoc :collections collections)
            (when (:reindex? opt true)
              (index-content app {:ids (search/get-diff-keys (->diffable collections) (->diffable old-collections))})))))

      (add-tap render-scene)

      (js/window.addEventListener
       "message"
       (fn [e]
         (when (.. e -data -action)
           (when-let [action (actions/get-action (.-data e))]
             (actions/execute-action! app action)))))))

  (when-not (client/started? app)
    (index-content app))

  (client/start-app app {:on-render on-render}))
