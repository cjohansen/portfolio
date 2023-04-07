(ns portfolio.ui.routes
  (:require [portfolio.ui.router :as router]))

(defn get-location [location item]
  (assoc location :query-params {:id (:id item)}))

(defn get-url [location item]
  (router/get-url (get-location location item)))

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
