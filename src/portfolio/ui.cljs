(ns portfolio.ui
  (:require [portfolio.actions :as actions]
            [portfolio.client :as client]
            [portfolio.collection :as collection]
            [portfolio.data :as data]
            [portfolio.homeless :as h]
            [portfolio.views.canvas :as canvas]
            [portfolio.views.canvas.background :as canvas-bg]
            [portfolio.views.canvas.grid :as canvas-grid]
            [portfolio.views.canvas.split :as split]
            [portfolio.views.canvas.viewport :as canvas-vp]
            [portfolio.views.canvas.zoom :as canvas-zoom]))

(def app (atom nil))

(defn get-collections [scenes collections]
  (collection/get-default-organization (vals scenes) (vals collections)))

(defn create-app [config canvas-tools extra-canvas-tools]
  (-> config
      (assoc :scenes @data/scenes)
      (assoc :collections (get-collections @data/scenes @data/collections))
      (assoc :views [(canvas/create-canvas
                      {:canvas/layout (:canvas/layout config)
                       :tools (into (or canvas-tools
                                        [(canvas-bg/create-background-tool config)
                                         (canvas-vp/create-viewport-tool config)
                                         (canvas-grid/create-grid-tool config)
                                         (canvas-zoom/create-zoom-tool config)
                                         (split/create-split-horizontally-tool config)
                                         (split/create-split-vertically-tool config)
                                         (split/create-close-tool config)])
                                    extra-canvas-tools)})])))

(def eventually-execute (h/debounce actions/execute-action! 250))

(defn start! [& [{:keys [on-render config canvas-tools extra-canvas-tools]}]]
  (swap! app merge (create-app config canvas-tools extra-canvas-tools))

  (add-watch data/scenes ::app
    (fn [_ _ _ scenes]
      (swap! app (fn [state]
                   (-> state
                       (assoc :scenes scenes)
                       (assoc :collections (get-collections scenes (:collections @app))))))
      (eventually-execute app [:go-to-current-location])))

  (add-watch data/collections ::app
    (fn [_ _ _ collections]
      (swap! app (fn [state]
                   (assoc state :collections (get-collections (:scenes state) collections))))))

  (client/start-app app {:on-render on-render}))
