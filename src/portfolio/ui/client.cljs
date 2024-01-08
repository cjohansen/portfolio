(ns portfolio.ui.client
  "Bootstrap and render a Portfolio UI app instance"
  (:require [clojure.edn :as edn]
            [dumdom.core :as d]
            [goog.object :as o]
            [portfolio.adapter :as adapter]
            [portfolio.homeless :as h]
            [portfolio.ui.actions :as actions]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.components.app :refer [App]]
            [portfolio.ui.core :as portfolio]
            [portfolio.ui.css :as css]
            [portfolio.ui.routes :as routes]))

(defn render [app {:keys [on-render]}]
  (let [state @app
        page-data (portfolio/prepare-data state (:location state))
        app-data (actions/actionize-data app page-data)]
    (when (ifn? on-render)
      (on-render page-data))
    (if-let [el (js/document.getElementById "portfolio")]
      (d/render (App app-data) el)
      (js/console.error "Unable to render portfolio: no element with id \"portfolio\""))))

(defn- a-element [el]
  (loop [el el]
    (cond
      (nil? el) nil
      (and (.-href el)
           (= "A" (.-tagName el))) el
      :else (recur (.-parentNode el)))))

(defn- get-path [href]
  (when (not-empty href)
    (.replace href js/location.origin "")))

(defn relay-body-clicks [app e]
  (let [path (some->> (.-target e) a-element .-href get-path)]
    (when (and path (re-find #"^/" path))
      (.preventDefault e)
      (if (or e.ctrlKey e.metaKey)
        (.open js/window path "_blank")
        (do
          (when (:log? @app)
            (println "Update URL from body click" path))
          (.pushState js/history false false path)
          (actions/execute-action! app [:go-to-current-location]))))))

(defn ensure-portfolio-css! [f]
  (if-not (js/document.getElementById "portfolio-css")
    (let [el (css/create-css-link "/portfolio/styles/portfolio.css")]
      (.addEventListener el "load" (fn listener [e]
                                     (.removeEventListener el "load" listener)
                                     (f)))
      (.appendChild js/document.head el))
    (f)))

(defn ensure-element! []
  (when-not (js/document.getElementById "portfolio")
    (let [el (js/document.createElement "div")]
      (set! (.-id el) "portfolio")
      (.appendChild js/document.body el))
    (let [script (js/document.createElement "script")]
      (set! (.-type script) "text/javascript")
      (set! (.-src script) "/portfolio/prism.js")
      (.appendChild js/document.body script))))

(defn set-window-size [app]
  (let [dim {:w js/window.innerWidth
             :h js/window.innerHeight}]
    (swap! app assoc :win dim)))

(def ^:private set-window-size-debounced (h/debounce set-window-size 100))

(defn keep-size-up-to-date [app]
  (set-window-size app)
  (set! js/window.onresize #(set-window-size-debounced app)))

(defn keep-css-files-up-to-date [app]
  (when-not (::css-listener @app)
    (let [observer (css/watch-css-reloads (:css-paths @app))]
      (swap! app assoc ::css-listener observer))))

(defn started? [app]
  (::started? @app))

(defn start-app [app & [{:keys [on-render]}]]
  (css/load-css-files (:css-paths @app))
  (if (started? app)
    (render app {:on-render on-render})
    (do
      (js/document.body.addEventListener "click" #(relay-body-clicks app %))
      (keep-size-up-to-date app)
      (keep-css-files-up-to-date app)
      (ensure-element!)
      (ensure-portfolio-css!
       (fn []
         (set! js/window.onpopstate (fn [] (actions/execute-action! app [:go-to-current-location])))
         (add-tap #(swap! app update :taps conj %))
         (add-watch app ::render (fn [_ _ _ _] (render app {:on-render on-render})))
         (actions/execute-action!
          app
          (let [location (routes/get-current-location)]
            (if (nil? (collection/get-selection @app (routes/get-id location)))
              (if-let [id (:id (first (sort-by :id (vals (:scenes @app)))))]
                [:go-to-location {:query-params {:id id}}]
                [:go-to-location
                 (cond-> location
                   (nil? (-> location :query-params :doc))
                   (assoc :query-params {:doc "up-and-running"}))])
              [:go-to-current-location])))
         (swap! app assoc ::started? true)))))
  app)

(defn mark-embed-ready!
  [app]
  (set! (.-portfolioReady js/window) true)
  (swap! app assoc ::started? true)
  (js/window.parent.postMessage #js {:portfolio_ready true} "*"))

(defn track-host-location
  [app]
  (js/window.addEventListener
    "message"
    (fn [e]
      (let [message (.-data e)]
        (when-let [scene-id (o/get message "set_scene")]
          (swap! app assoc ::opt (some-> message (o/get "opt") edn/read-string))
          (actions/execute-action! app [:go-to-location {:query-params {:id scene-id
                                                                        :portfolio.embed true}}]))))))

(defn render-component [app {:keys [on-render]}]
  (let [state @app]
    (when (ifn? on-render)
      (on-render
        ;; TODO: add page-data parameter
        #_page-data))
    (if-let [el (js/document.getElementById "portfolio")]
      (when-let [{:keys [target]} (collection/get-selection state (routes/get-id (:location state)))]
        (adapter/render-component (assoc target :component ((:component-fn target) nil (::opt state))) el)
        (js/window.parent.postMessage #js {:portfolio_render (routes/get-id (:location state))}))
      (js/console.error "Unable to render portfolio: no element with id \"portfolio\""))))

(defn start-embed-app [app & [{:keys [on-render]}]]
  (css/load-css-files-direct (:css-paths @app))
  (if (started? app)
    (render-component app {:on-render on-render})
    (do
      (ensure-element!)
      (track-host-location app)
      (add-watch app ::render (fn [_ _ _ _] (render-component app {:on-render on-render})))
      (mark-embed-ready! app)))
  app)
