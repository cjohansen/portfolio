(ns portfolio.views.canvas
  (:require [portfolio.components.canvas :refer [CanvasView]]
            [portfolio.protocols :as portfolio]))

(def view-impl
  {`portfolio/render-view #'CanvasView})

(defn get-expand-path [vid]
  [:canvas/tools vid :expanded])

(defn prepare-tool-menu [vid tool current-value]
  {:options
   (for [{:keys [title value]} (:options tool)]
     (let [selected? (= value current-value)]
       {:title title
        :selected? selected?
        :actions [[:dissoc-in (get-expand-path vid)]
                  (if selected?
                    [:dissoc-in [(:id tool) vid :value]]
                    [:assoc-in [(:id tool) vid :value] value])]}))})

(defn get-current-layout [state view]
  (if (= 1 (count (:current-scenes state)))
    (or (when-let [layout (-> state :current-scenes first :canvas/layout)]
          {:layout layout
           :source (-> state :current-scenes first :id)})
        (when-let [layout (-> state :current-namespace :canvas/layout)]
          {:layout layout
           :source (-> state :current-namespace :namespace)})
        (when-let [layout (-> state :current-collection :canvas/layout)]
          {:layout layout
           :source (-> state :current-collection :id)})
        (when-let [layout (:canvas/layout state)]
          {:layout layout
           :source :state-layout})
        (when-let [layout (:canvas/layout view)]
          {:layout layout
           :source (:id view)}))
    {:layout [[{:viewport/height :auto}]]
     :source ::multi-scene-default}))

(defn prepare-toolbar [state vid tools overrides]
  (let [expand-path (get-expand-path vid)
        expanded (get-in state expand-path)]
    {:tools
     (map (fn [tool value]
            (let [expanded? (= (:id tool) expanded)]
              (assoc tool
                     :actions (if expanded?
                                [[:dissoc-in expand-path]]
                                [[:assoc-in expand-path (:id tool)]])
                     :active? (boolean value)
                     :menu (when expanded?
                             (prepare-tool-menu vid tool value)))))
          tools overrides)}))

(defn get-current-addon [location addons]
  (or (when-let [id (some-> location :query-params :addon keyword)]
        (first (filter (comp #{id} :id) addons)))
      (first addons)))

(defn prepare-panel [state location scene addons]
  (let [current-addon (get-current-addon location addons)
        minimize-path [(:id scene) :minimize-panel?]]
    {:tabs (for [addon addons]
             (cond-> addon
               (= current-addon addon)
               (assoc :selected? true)))
     :minimized? (get-in state minimize-path)
     :button (if (get-in state minimize-path)
               {:text "Maximize"
                :actions [[:dissoc-in minimize-path]]
                :direction :up}
               {:text "Minimize"
                :direction :down
                :actions [[:assoc-in minimize-path true]]})
     :content (portfolio/prepare-addon-content current-addon state location scene)}))

(defn prepare-canvas-view [view state location]
  (let [layout (get-current-layout state view)
        scenes (:current-scenes state)]
    (with-meta
      {:rows
       (for [[row y] (map vector (:layout layout) (range))]
         (for [[opt x] (map vector row (range))]
           (let [vid [(:source layout) x y]
                 overrides (map #(portfolio/get-local-overrides % state vid) (:tools view))]
             (when (seq scenes)
               {:toolbar (prepare-toolbar state vid (:tools view) overrides)
                :canvases (for [canvas scenes]
                            {:scene canvas
                             :tools (:tools view)
                             :opt (apply merge opt overrides)})}))))
       :panel (when (and (= 1 (count scenes)) (seq (:addons view)))
                (prepare-panel state location (first scenes) (:addons view)))}
      view-impl)))

(def data-impl
  {`portfolio/prepare-data #'prepare-canvas-view})

(defn create-canvas [{:keys [tools addons layout]}]
  (-> {:id ::canvas
       :title "Canvas"
       :tools tools
       :addons addons
       :layout (or layout [[{}]])}
      (with-meta data-impl)))
