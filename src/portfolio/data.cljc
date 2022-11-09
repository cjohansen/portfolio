(ns portfolio.data)

(def scenes (atom {}))
(def namespaces (atom {}))
(def collections (atom {}))

(defn register-scene! [scene]
  (if-not (:id scene)
    (throw (ex-info "Cannot register scene without :id" {:scene scene}))
    (swap! scenes assoc (:id scene) (cond-> scene
                                      (empty? (:title scene))
                                      (assoc :title (name (:id scene)))))))

(defn register-namespace! [ns]
  (if-not (:namespace ns)
    (throw (ex-info "Cannot register namespace without :namespace" {:namespace ns}))
    (swap! namespaces assoc (:namespace ns) ns)))

(defn register-collection! [collection]
  (if-not (:id collection)
    (throw (ex-info "Cannot register collection without :id" {:id collection}))
    (swap! collections assoc (:id collection) collection)))
