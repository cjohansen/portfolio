(ns portfolio.ui.client
  "Bootstrap and render a Portfolio UI app instance"
  (:require [dumdom.core :as d]
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

(defn add-once-listener [el event f]
  (.addEventListener el event (fn listener [_e]
                                (.removeEventListener el event listener)
                                (f))))

(defn add-load-listener [el f & [error]]
  (.addEventListener
   el "load"
   (fn listener [_e]
     (.removeEventListener el "load" listener)
     (.removeEventListener el "error" listener)
     (f)))
  (.addEventListener
   el "error"
   (fn listener [_e]
     (.removeEventListener el "load" listener)
     (.removeEventListener el "error" listener)
     (if error
       (set! (.-innerHTML js/document.body) error)
       (f)))))

(def css-file "/portfolio/styles/portfolio.css")

(def css-load-error
  (str
   "<h1>Unable to load the Portfolio CSS</h1>"
   "<p>Portfolio needs to load its CSS file " css-file
   "in order to render its UI. Make sure Portfolio's resources are served from "
   "your web server. If you are using shadow-cljs, you'll need something like "
   "the following:</p>"
   "<pre><code>:dev-http {8080 [\"public\" \"classpath:public\"]}</code></pre>"))

(defn ensure-portfolio-css! [f]
  (if-not (js/document.getElementById "portfolio-css")
    (let [el (css/create-css-link css-file)]
      (add-load-listener el f css-load-error)
      (.appendChild js/document.head el))
    (f)))

(defn ensure-element! [f]
  (if-not (js/document.getElementById "portfolio")
    (let [el (js/document.createElement "div")
          script (js/document.createElement "script")]
      (set! (.-id el) "portfolio")
      (.appendChild js/document.body el)
      (set! (.-type script) "text/javascript")
      (set! (.-src script) "/portfolio/prism.js")
      (add-once-listener script "load" f)
      (.appendChild js/document.body script))
    (f)))

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
  (let [f (fn []
            (css/load-css-files (:css-paths @app))
            (if (started? app)
              (render app {:on-render on-render})
              (do
                (js/document.body.addEventListener "click" #(relay-body-clicks app %))
                (keep-size-up-to-date app)
                (keep-css-files-up-to-date app)
                (ensure-element!
                 (fn []
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
                      (swap! app assoc ::started? true))))))))]
    ;; Portfolio used to just start at will, and would crash when loaded from
    ;; <head> This little indirection means Portfolio still starts synchronously
    ;; when loaded from <body>, but now also starts when being loaded in <head>,
    ;; although asynchronously.
    (if (and js/document js/document.body)
      (f)
      (add-once-listener js/window "DOMContentLoaded" f)))
  app)
