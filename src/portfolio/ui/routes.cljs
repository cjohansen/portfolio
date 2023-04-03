(ns portfolio.ui.routes
  (:require [portfolio.ui.router :as router]))

(defn get-scene-url [location scene]
  (-> location
      (assoc :query-params {:id (:id scene)})
      router/get-url))

(defn get-addon-url [location addon]
  (-> location
      (assoc-in [:query-params :addon] (:id addon))
      router/get-url))

(defn get-id [location]
  (some-> location :query-params :id keyword))
