(ns portfolio.ui.router
  (:require [clojure.string :as str]))

(defn parse-qs-val [v]
  (let [v (js/decodeURIComponent v)]
    (cond
      (re-find #"^\d+$" v) (js/parseInt v 10)
      (re-find #"^\d+\.\d+$" v) (js/parseFloat v)
      (= "true" v) true
      (= "false" v) false
      :else v)))

(defn parse-key [k]
  (cond
    (re-find #"__" k) (->> (str/split k #"__")
                           (mapv parse-key))
    (re-find #"^\d+$" k) (js/parseInt k 10)
    :else (keyword k)))

(defn parse-query-params
  "Parse a query string into a map with keyword keys. Query params that have no
  value (e.g. `...&key&other-key`) will be parsed with `true` as the value."
  [query-string]
  (some->> (str/split query-string #"&")
           (remove empty?)
           seq
           (map (fn [s]
                  (if (re-find #"=" s)
                    (let [[k & v] (str/split s #"=")]
                      [(parse-key k) (parse-qs-val (str/join "=" v))])
                    [(parse-key s) true])))
           (into {})))

(defn get-location [url]
  (let [[path query] (str/split url #"\?")]
    (cond-> {:path path}
      (string? query) (assoc :query-params (parse-query-params query)))))

(defn get-current-url []
  (js/window.location.href.replace js/window.location.origin ""))

(defn get-current-location []
  (get-location (get-current-url)))

(defn- blank? [v]
  (or (nil? v)
      (false? v)
      (and (coll? v) (empty? v))
      (= v "")))

(defn stringify-key [k]
  (cond
    (keyword? k) (str (when-let [ns (namespace k)]
                        (str ns "/")) (name k))
    (vector? k) (->> (map stringify-key k)
                     (str/join "__"))
    :else k))

(defn encode-query-params
  "Encode a map as a query string. Empty values (nil, empty strings, empty
  collections, false values) are omitted from the resulting string."
  [params]
  (if (empty? params)
    ""
    (->> params
         (remove (comp blank? second))
         (map (fn [[k v]]
                (let [k (stringify-key k)]
                  (cond
                    (true? v) k
                    (keyword? v) (str k "=" (js/encodeURIComponent
                                             (str (when-let [ns (namespace v)]
                                                    (str ns "/")) (name v))))
                    :else (str k "=" (js/encodeURIComponent v))))))
         (str/join "&"))))

(defn get-url [location]
  (let [qs (encode-query-params (:query-params location))]
    (str (:path location) (when-not (empty? qs) (str "?" qs)))))
