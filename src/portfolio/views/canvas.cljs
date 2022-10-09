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
  (or (when-let [layout (-> state :current-scene :canvas/layout)]
        {:layout layout
         :source (-> state :current-scene :id)})
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
         :source (:id view)})))

(defn prepare-canvas-view [view state _]
  (let [layout (get-current-layout state view)]
    (with-meta
      {:rows
       (for [[row y] (map vector (:layout layout) (range))]
         (for [[opt x] (map vector row (range))]
           (let [vid [(:source layout) x y]
                 overrides (map #(portfolio/get-local-overrides % state vid) (:tools view))
                 expand-path (get-expand-path vid)
                 expanded (get-in state expand-path)]
             {:toolbar
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
                    (:tools view)
                    overrides)}
              :canvas {:current-scene (:current-scene state)
                       :tools (:tools view)
                       :opt (apply merge opt overrides)}})))}
      view-impl)))

(def data-impl
  {`portfolio/prepare-data #'prepare-canvas-view})

(defn create-canvas [{:keys [tools layout]}]
  (-> {:id ::canvas
       :title "Canvas"
       :tools tools
       :layout (or layout [[{}]])}
      (with-meta data-impl)))
