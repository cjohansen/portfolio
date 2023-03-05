(ns portfolio.ui
  (:require [clojure.string :as str]
            [portfolio.actions :as actions]
            [portfolio.client :as client]
            [portfolio.core :as portfolio]
            [portfolio.data :as data]
            [portfolio.homeless :as h]
            [portfolio.views.canvas :as canvas]
            [portfolio.views.canvas.background :as canvas-bg]
            [portfolio.views.canvas.grid :as canvas-grid]
            [portfolio.views.canvas.viewport :as canvas-vp]
            [portfolio.views.canvas.zoom :as canvas-zoom]))

(def app (atom nil))

(defn create-app [config canvas-tools extra-canvas-tools]
  (-> (assoc config
             :scenes (vals @data/scenes)
             :namespaces (vals @data/namespaces)
             :collections (vals @data/collections))
      portfolio/init-state
      (assoc :views [(canvas/create-canvas
                      {:canvas/layout (:canvas/layout config)
                       :tools (into (or canvas-tools
                                        [(canvas-bg/create-background-tool config)
                                         (canvas-vp/create-viewport-tool config)
                                         (canvas-grid/create-grid-tool config)
                                         (canvas-zoom/create-zoom-tool config)])
                                    extra-canvas-tools)})])))

(def eventually-execute (h/debounce actions/execute-action! 250))

(defn create-css-link [path & [{:keys [media]}]]
  (let [link (js/document.createElement "link")]
    (set! (.-href link) path)
    (set! (.-rel link) "stylesheet")
    (set! (.-type link) "text/css")
    (when media
      (set! (.-media link) "print"))
    link))

(defn reload-css-file [file]
  (doseq [iframe (.querySelectorAll js/document.body "iframe")]
    (let [iframe-head (some-> iframe .-contentWindow .-document .-head)
          original (->> (.querySelectorAll iframe-head "link")
                        (filter #(str/includes? (.-href %) file))
                        first)
          reloaded (create-css-link (str file "?" (.getTime (js/Date.))))]
      (.addEventListener
       reloaded
       "load"
       (fn done [_]
         (when-let [parent (some-> original .-parentNode)]
           (.removeChild parent original))
         (.removeEventListener reloaded "load" done)))
      (.appendChild iframe-head reloaded))))

(defn start! [& [{:keys [on-render config canvas-tools extra-canvas-tools]}]]
  (swap! app merge (create-app config canvas-tools extra-canvas-tools))
  (add-watch data/scenes ::app (fn [_ _ _ scenes]
                                 (swap! app assoc :scenes scenes)
                                 (eventually-execute app [:go-to-current-location])))
  (add-watch data/namespaces ::app (fn [_ _ _ namespaces] (swap! app assoc :namespaces namespaces)))
  (add-watch data/collections ::app (fn [_ _ _ collections] (swap! app assoc :collections collections)))
  (client/start-app app {:on-render on-render})

  (doseq [path (:css-paths config)]
    (.appendChild js/document.head (create-css-link path {:media "print"})))

  (when-let [listener (::css-listener @app)]
    (.removeEventListener js/document.body "figwheel.after-css-load" listener))

  (.addEventListener
   js/document.body
   "figwheel.after-css-load"
   (fn css-listener [e]
     (swap! app assoc ::css-listener css-listener)
     (doseq [file (:css-files (.-data e))]
       (->> config
            :css-paths
            (filter #(str/includes? file %))
            first
            reload-css-file)))))
