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
    (d/render (App app-data) (js/document.getElementById "app"))))

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

(defn start-app [app & [{:keys [on-render]}]]
  (js/document.body.addEventListener "click" #(relay-body-clicks app %))
  (set! js/window.onpopstate (fn [] (actions/execute-action! app [:go-to-current-location])))
  (add-watch app ::render (fn [_ _ _ _] (render app {:on-render on-render})))
  (actions/execute-action!
   app
   (if (empty? (:query-params (router/get-current-location)))
     [:go-to-location {:query-params {:scene (:id (first (sort (vals (:scenes @app)))))}}]
     [:go-to-current-location]))
  app)
