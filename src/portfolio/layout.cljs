(ns portfolio.layout
  (:require [portfolio.core :as portfolio]))

(defn gallery? [state location]
  (or (contains? (:query-params location) :namespace)
      (< 1 (count (:current-scenes state)))))

(defn assign-pane-ids [layout]
  (if (:kind layout)
    (update layout :xs #(mapv assign-pane-ids %))
    (assoc layout ::pane-id (str (random-uuid)))))

(defn init-layout [state layout path]
  (or (get-in state [:layout path])
      {:layout (assign-pane-ids layout)
       :source path}))

(defn get-current-layout-path []
  [:layout :current])

(defn get-layout-path [layout]
  [:layout (:source layout)])

(defn get-view-layout [state location]
  (if (gallery? state location)
    (-> (init-layout
         state
         (merge {:viewport/height :auto} (:canvas/gallery-defaults state))
         [::gallery-default])
        (assoc :gallery? true))
    (let [scenes (portfolio/get-current-scenes state location)
          ns (portfolio/get-scene-namespace state (first scenes))]
      (or (when-let [scene (first (filter :canvas/layout scenes))]
            (init-layout state (:canvas/layout scene) [:scene (:id scene)]))
          (when-let [layout (:canvas/layout ns)]
            (init-layout state layout [:namespace (:namespace ns)]))
          (let [collection (portfolio/get-collection state (:collection ns))]
            (when-let [layout (:canvas/layout collection)]
              (init-layout state layout [:collection (:id collection)])))
          (when-let [layout (:canvas/layout state)]
            (init-layout state layout [:state-layout]))
          (when-let [view (first (filter :canvas/layout (:views state)))]
            (init-layout state (:canvas/layout view) [:view (:id view)]))
          (init-layout state {} [:layout/default])))))

(defn get-current-layout [state]
  (get-in state (get-in state (get-current-layout-path))))
