(ns portfolio.views.canvas
  (:require [portfolio.components.canvas :refer [CanvasView]]
            [portfolio.core :as p]
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

(defn multi-scene? [state location]
  (or (contains? (:query-params location) :namespace)
      (< 1 (count (:current-scenes state)))))

(defn get-current-layout [state location view]
  (if (multi-scene? state location)
    {:layout [[{:viewport/height :auto}]]
     :source ::multi-scene-default}
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
           :source (:id view)}))))

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
        minimize-path [(:id scene) :minimize-panel?]
        content (portfolio/prepare-addon-content current-addon state location scene)
        minimized? (get-in state minimize-path (not content))]
    {:tabs (for [addon addons]
             (cond-> addon
               (= current-addon addon)
               (assoc :selected? true)))
     :minimized? minimized?
     :button (if minimized?
               {:text "Maximize"
                :actions [[:dissoc-in minimize-path]]
                :direction :up}
               {:text "Minimize"
                :direction :down
                :actions [[:assoc-in minimize-path true]]})
     :content content}))

(defn prepare-canvas-view [view state location]
  (let [layout (get-current-layout state location view)
        scenes (:current-scenes state)
        multi? (multi-scene? state location)]
    (with-meta
      {:mode (if multi? (:namespace (:current-namespace state)) :single-scene)
       :rows
       (for [[row y] (map vector (:layout layout) (range))]
         (for [[opt x] (map vector row (range))]
           (let [vid [(:source layout) x y]
                 overrides (map #(portfolio/get-local-overrides % state vid) (:tools view))]
             (when (seq scenes)
               {:toolbar (prepare-toolbar state vid (:tools view) overrides)
                :canvases
                (for [scene scenes]
                  (cond->
                      {:scene scene
                       :tools (:tools view)
                       :opt (apply merge opt overrides)}
                    multi?
                    (assoc :title (:title scene)
                           :url (p/get-scene-url location scene)
                           :description (:description scene))))}))))
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
