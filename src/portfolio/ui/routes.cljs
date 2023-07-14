(ns portfolio.ui.routes
  (:require [portfolio.ui.router :as router]))

(defn get-location [location item]
  (assoc location :query-params {:id (:id item)}))

(defn get-url
  ([location]
   (router/get-url location))
  ([location item]
   (router/get-url (get-location location item))))

(defn get-scene-location [location scene]
  (assoc location :query-params {:id (:id scene)}))

(defn get-scene-url [location scene]
  (router/get-url (get-scene-location location scene)))

(defn get-addon-url [location addon]
  (-> location
      (assoc-in [:query-params :addon] (:id addon))
      router/get-url))

(defn get-id [location]
  (some-> location :query-params :id keyword))

(defn get-document-id [location]
  (when-let [doc (-> location :query-params :doc)]
    (keyword "document" doc)))

(defn get-current-url []
  (js/window.location.href.replace js/window.location.origin ""))

(defn get-current-location []
  (router/get-location (get-current-url)))
