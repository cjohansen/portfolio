(ns portfolio.views.canvas.addons
  (:require [portfolio.components.canvas-toolbar-buttons :refer [MenuButton]]
            [portfolio.protocols :as portfolio]
            [portfolio.views.canvas.protocols :as protocols]))

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

(defn prepare-toolbar-menu-button [tool state {:keys [pane-id]}]
  (let [expand-path (get-expand-path pane-id)
        expanded? (= (:id tool) (get-in state expand-path))
        value (portfolio/get-local-overrides tool state pane-id)]
    (with-meta
      {:text (:title tool)
       :actions (if expanded?
                  [[:dissoc-in expand-path]]
                  [[:assoc-in expand-path (:id tool)]])
       :active? (boolean value)
       :menu (when expanded?
               (prepare-tool-menu pane-id tool value))}
      {`protocols/render-toolbar-button #'MenuButton})))

(defn create-toolbar-menu-button [data]
  (doseq [k #{:id :title :options :prepare-layer}]
    (when-not (k data)
      (throw (ex-info "Can't create toolbar menu button without key"
                      {:k k :data data}))))
  (with-meta
    (dissoc data :prepare-layer)
    {`protocols/prepare-toolbar-button #'prepare-toolbar-menu-button
     `portfolio/prepare-layer (:prepare-layer data)}))

(defn create-action-button [data]
  (doseq [k #{:title :get-actions :prepare-layer}]
    (when-not (k data)
      (throw (ex-info "Can't create toolbar action button without key"
                      {:k k :data data}))))
  (let [show? (or (:show? data) (constantly true))]
    (with-meta
      (dissoc data :show? :get-actions :prepare-layer)
      {`portfolio/prepare-layer (:prepare-layer data)
       `protocols/prepare-toolbar-button
       (fn [tool state options]
         (when (show? tool state options)
           (with-meta
             {:text (:title data)
              :actions ((:get-actions data) tool state options)}
             {`protocols/render-toolbar-button #'MenuButton})))})))
