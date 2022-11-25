(ns portfolio.adapter)

(defprotocol IComponentLibrary
  :extend-via-metadata true
  (render-component [component el]))

(defn prepare-scene [scene component-impl]
  (cond-> scene
    (:component scene)
    (update :component #(with-meta {:id (:id scene)
                                    :component %} component-impl))

    (:component-fn scene)
    (update :component-fn
            (fn [f]
              (fn [data]
                (with-meta {:id (:id scene)
                            :component (f data)} component-impl))))))
