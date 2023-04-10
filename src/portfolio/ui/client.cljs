(ns portfolio.ui.client
  "Bootstrap and render a Portfolio UI app instance"
  (:require [clojure.string :as str]
            [dumdom.core :as d]
            [portfolio.homeless :as h]
            [portfolio.ui.actions :as actions]
            [portfolio.ui.components.app :refer [App]]
            [portfolio.ui.core :as portfolio]
            [portfolio.ui.css :as css]
            [portfolio.ui.router :as router]))

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
    (.addEventListener
     js/document.body
     "figwheel.after-css-load"
     (fn css-listener [e]
       (swap! app assoc ::css-listener css-listener)
       (doseq [file (:css-files (.-data e))]
         (->> (:css-paths @app)
              (filter #(str/includes? file %))
              first
              css/reload-css-file))))))

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
          (if (empty? (:query-params (router/get-current-location)))
            [:go-to-location {:query-params {:id (:id (first (sort-by :id (vals (:scenes @app)))))}}]
            [:go-to-current-location]))
         (swap! app assoc ::started? true)))))
  app)
