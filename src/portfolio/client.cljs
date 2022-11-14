(ns portfolio.client
  "Bootstrap and render a Portfolio UI app instance"
  (:require [dumdom.core :as d]
            [portfolio.actions :as actions]
            [portfolio.components.app :refer [App]]
            [portfolio.core :as portfolio]
            [portfolio.router :as router]))

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
      :default (recur (.-parentNode el)))))

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
    (let [el (js/document.createElement "link")]
      (set! (.-rel el) "stylesheet")
      (set! (.-type el) "text/css")
      (set! (.-href el) "/portfolio/styles/portfolio.css")
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

(defn start-app [app & [{:keys [on-render]}]]
  (if (::started? @app)
    (render app {:on-render on-render})
    (do
      (js/document.body.addEventListener "click" #(relay-body-clicks app %))
      (ensure-element!)
      (ensure-portfolio-css!
       (fn []
         (set! js/window.onpopstate (fn [] (actions/execute-action! app [:go-to-current-location])))
         (add-tap #(swap! app update :taps conj %))
         (add-watch app ::render (fn [_ _ _ _] (render app {:on-render on-render})))
         (actions/execute-action!
          app
          (if (empty? (:query-params (router/get-current-location)))
            [:go-to-location {:query-params {:scene (:id (first (sort-by :id (vals (:scenes @app)))))}}]
            [:go-to-current-location]))
         (swap! app assoc ::started? true)))))
  app)
