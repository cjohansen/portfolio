(ns portfolio.views.canvas.addons
  (:require [portfolio.components.canvas-toolbar-buttons :refer [MenuButton]]
            [portfolio.views.canvas.protocols :as canvas]))

(defn get-expand-path [vid]
  [:canvas/tools vid :expanded])

(defn get-custom-tool-source-title [[source]]
  (case source
    :scene "Scene config"
    :namespace "NS config"
    :collection "Collection config"
    :state-layout "Global config"
    :view "Canvas config"
    :layout/default "Default config"))

(defn prepare-tool-menu [tool state {:keys [pane-id pane-options config-source]}]
  (let [selected-value (canvas/get-tool-value tool state pane-id)
        value (or selected-value (:default-value tool))
        current-value (and (map? value)
                           (not-empty (select-keys pane-options (keys value))))
        custom-options (when (and current-value (not= current-value value))
                         [{:title (get-custom-tool-source-title config-source)
                           :value current-value
                           :disabled? true}])]
    {:options
     (for [{:keys [title value disabled?]} (concat (:options tool) custom-options)]
       (let [selected? (= value current-value)]
         {:title title
          :selected? selected?
          :actions (when-not disabled?
                     [[:dissoc-in (get-expand-path pane-id)]
                      (if selected?
                        [:dissoc-in [(:id tool) pane-id :value]]
                        [:assoc-in [(:id tool) pane-id :value] value])])}))}))

(defn prepare-toolbar-menu-button [tool state pane]
  (let [expand-path (get-expand-path (:pane-id pane))
        expanded? (= (:id tool) (get-in state expand-path))]
    (with-meta
      {:text (:title tool)
       :actions (if expanded?
                  [[:dissoc-in expand-path]]
                  [[:assoc-in expand-path (:id tool)]])
       :menu (when expanded?
               (prepare-tool-menu tool state pane))}
      {`canvas/render-toolbar-button #'MenuButton})))

(defn create-toolbar-menu-button [data]
  (doseq [k #{:id :title :options :prepare-canvas}]
    (when-not (k data)
      (throw (ex-info "Can't create toolbar menu button without key"
                      {:k k :data data}))))
  (with-meta
    (dissoc data :prepare-canvas)
    {`canvas/prepare-toolbar-button #'prepare-toolbar-menu-button
     `canvas/prepare-canvas (or (:prepare-canvas data) (fn [_ _ _]))
     `canvas/finalize-canvas (or (:finalize-canvas data) (fn [_ _ _]))}))

(defn create-action-button [data]
  (doseq [k #{:title :get-actions :prepare-canvas}]
    (when-not (k data)
      (throw (ex-info "Can't create toolbar action button without key"
                      {:k k :data data}))))
  (let [show? (or (:show? data) (constantly true))]
    (with-meta
      (dissoc data :show? :get-actions :prepare-canvas)
      {`canvas/prepare-canvas (or (:prepare-canvas data) (fn [_ _ _]))
       `canvas/finalize-canvas (or (:finalize-canvas data) (fn [_ _ _]))
       `canvas/prepare-toolbar-button
       (fn [tool state options]
         (when (show? tool state options)
           (with-meta
             {:text (when-not (:icon data) (:title data))
              :title (:title data)
              :icon (:icon data)
              :actions ((:get-actions data) tool state options)}
             {`canvas/render-toolbar-button #'MenuButton})))})))
