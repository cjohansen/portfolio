(ns portfolio.ui.layout)

(defn gallery? [selection]
  (not= :scene (:kind selection)))

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

(defn get-view-layout [state selection]
  (if (gallery? selection)
    (-> (init-layout
         state
         (merge {:viewport/height :auto} (:canvas/gallery-defaults state))
         [::gallery-default])
        (assoc-in [:layout :gallery?] true))
    (or (when-let [scene (first (filter :canvas/layout (:scenes selection)))]
          (init-layout state (:canvas/layout scene) [:scene (:id scene)]))
        (when-let [collection (->> (reverse (:path selection))
                                   (filter :canvas/layout)
                                   first)]
          (init-layout state (:canvas/layout collection) [:collection (:id collection)]))
        (when-let [layout (:canvas/layout state)]
          (init-layout state layout [:state-layout]))
        (when-let [view (first (filter :canvas/layout (:views state)))]
          (init-layout state (:canvas/layout view) [:view (:id view)]))
        (init-layout state {} [::default]))))

(defn get-layout [state path]
  (get-in state path))

(defn get-current-layout [state]
  (get-in state (get-in state (get-current-layout-path))))

(defn get-panes [layout]
  (if (#{:rows :cols} (:kind layout))
    (mapcat get-panes (:xs layout))
    [layout]))

(defn get-layout-panes [{:keys [layout]}]
  (get-panes layout))
