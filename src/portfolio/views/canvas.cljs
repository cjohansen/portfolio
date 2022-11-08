(ns portfolio.views.canvas
  (:require [portfolio.components.canvas :refer [CanvasView]]
            [portfolio.core :as p]
            [portfolio.router :as router]
            [portfolio.view :as view]
            [portfolio.views.canvas.protocols :as canvas]))

(def view-impl
  {`view/render-view #'CanvasView})

(extend-type cljs.core/PersistentArrayMap
  canvas/ICanvasToolValue
  (get-tool-value [tool state canvas-id]
    (get-in state [(:id tool) canvas-id :value])))

(defn multi-scene? [state location]
  (or (contains? (:query-params location) :namespace)
      (< 1 (count (:current-scenes state)))))

(defn get-layout [state layout path]
  {:layout (or (get-in state [:layout path]) layout)
   :source path})

(defn get-current-layout [state location view]
  (if (multi-scene? state location)
    (get-layout state [[{:viewport/height :auto}]] [::multi-scene-default])
    (or (when-let [layout (-> state :current-scenes first :canvas/layout)]
          (get-layout state layout [:scene (-> state :current-scenes first :id)]))
        (when-let [layout (-> state :current-namespace :canvas/layout)]
          (get-layout state layout [:namespace (-> state :current-namespace :namespace)]))
        (when-let [layout (-> state :current-collection :canvas/layout)]
          (get-layout state layout [:collection (-> state :current-collection :id)]))
        (when-let [layout (:canvas/layout state)]
          (get-layout state layout [:state-layout]))
        (when-let [layout (:canvas/layout view)]
          (get-layout state layout [:view (:id view)]))
        (get-layout state [[{}]] [:layout/default]))))

(defn get-current-addon [location addons]
  (or (when-let [id (some-> location :query-params :addon keyword)]
        (first (filter (comp #{id} :id) addons)))
      (first addons)))

(defn prepare-panel [state location scene addons]
  (let [current-addon (get-current-addon location addons)
        minimize-path [:canvas/panel :minimized?]
        content (canvas/prepare-panel-content current-addon state scene)
        minimized? (get-in state minimize-path (not content))]
    {:tabs (for [addon addons]
             (cond-> addon
               (= current-addon addon)
               (assoc :selected? true)

               (not= current-addon addon)
               (assoc :url (-> location
                               (assoc-in [:query-params :addon] (:id addon))
                               router/get-url))))
     :minimized? minimized?
     :button (if minimized?
               {:text "Maximize"
                :actions [[:dissoc-in minimize-path]]
                :direction :up}
               {:text "Minimize"
                :direction :down
                :actions [[:assoc-in minimize-path true]]})
     :content content}))

(defn prepare-rows [state root-layout source view scenes path layout]
  (for [[row y] (map vector layout (range))]
     (for [[opt x] (map vector row (range))]
       (let [path (into path [y x])]
         (if (vector? opt)
           {:layout (prepare-rows state root-layout source view scenes path opt)}
           (let [id (concat source path)
                 options (->> (:tools view)
                              (map #(canvas/get-tool-value % state id))
                              (apply merge opt))]
             (when (seq scenes)
               {:toolbar
                {:tools
                 (->> (:tools view)
                      (keep #(canvas/prepare-toolbar-button
                              % state {:pane-id id
                                       :pane-options options
                                       :pane-path path
                                       :layout-path [:layout source]
                                       :layout root-layout})))}
                :canvases (map #(assoc % :opt options) scenes)})))))))

(defn prepare-layout [state location view {:keys [layout source]} scenes multi?]
  (let [scenes (for [scene scenes]
                 (cond->
                     {:scene scene
                      :tools (:tools view)}
                   multi?
                   (assoc :title (:title scene)
                          :url (p/get-scene-url location scene)
                          :description (:description scene))))]
    {:mode (if multi? (:namespace (:current-namespace state)) :single-scene)
     :rows (prepare-rows state layout source view scenes [] layout)}))

(defn prepare-canvas-view [view state location]
  (let [layout (get-current-layout state location view)
        scenes (:current-scenes state)
        multi? (multi-scene? state location)]
    (with-meta
      (assoc (prepare-layout state location view layout scenes multi?)
             :panel (when (and (= 1 (count scenes)) (seq (:addons view)))
                      (prepare-panel state location (first scenes) (:addons view))))
      view-impl)))

(def data-impl
  {`view/prepare-data #'prepare-canvas-view})

(defn create-canvas [{:keys [tools addons layout]}]
  (-> {:id ::canvas
       :title "Canvas"
       :tools tools
       :addons addons
       :layout (or layout [[{}]])}
      (with-meta data-impl)))
