(ns portfolio.ui.canvas.addons
  (:require [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]))

(defn get-expand-path [vid]
  [:canvas/tools vid :expanded])

(defn get-options-path [pane tool]
  [:panes (:pane-id pane) (or (:group-id tool) (:id tool)) :value])

(defn get-custom-tool-source-title [[source]]
  (case source
    :scene "Scene config"
    :namespace "NS config"
    :collection "Collection config"
    :state-layout "Global config"
    :view "Canvas config"
    :portfolio.views.canvas/gallery-default "Default config (gallery)"
    :layout/default "Default config"))

(defn get-current-value [tool state {:keys [pane-id pane-options]}]
  (let [selected-value (canvas/get-tool-value tool state pane-id)
        value (or selected-value (:default-value tool))]
    {:value value
     :current-value
     (and (map? value)
          (not-empty (select-keys pane-options (keys value))))}))

(defn prepare-tool-menu [tool state pane]
  (let [path (get-options-path pane tool)
        {:keys [value current-value]} (get-current-value tool state pane)
        custom-options (when (and current-value (not= current-value value))
                         [{:title (get-custom-tool-source-title (:config-source pane))
                           :value current-value
                           :disabled? true}])]
    {:options
     (for [{:keys [title value disabled?]} (concat (:options tool) custom-options)]
       (let [selected? (= value current-value)]
         {:title title
          :selected? selected?
          :actions (when-not disabled?
                     (->> [[:dissoc-in (get-expand-path (:pane-id pane))]
                           (if selected?
                             [:dissoc-in path]
                             [:assoc-in path value])
                           (when (ifn? (:on-select tool))
                             [:fn/call (:on-select tool) value])]
                          (remove nil?)))}))}))

(defn get-tool-title [state pane tool]
  (or (when (ifn? (:prepare-title tool))
        (let [f (:prepare-title tool)]
          (f (:current-value (get-current-value tool state pane)))))
      (:title tool)))

(defn prepare-toolbar-menu-button [tool state pane]
  (let [expand-path (get-expand-path (:pane-id pane))
        expanded? (= (:id tool) (get-in state expand-path))]
    (with-meta
      {:text (when-not (:icon tool)
               (get-tool-title state pane tool))
       :icon (:icon tool)
       :title (when (:icon tool)
                (get-tool-title state pane tool))
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

(defn create-canvas-extension [data]
  (assert (:id data) "Can't create viewport extension without :id")
  (assert (or (:prepare-canvas data)
              (:finalize-canvas data)) "Can't create viewport extension without neither :prepare-canvas nor :finalize-canvas")
  (with-meta
    data
    {`canvas/prepare-canvas (or (:prepare-canvas data) (fn [_ _ _]))
     `canvas/finalize-canvas (or (:finalize-canvas data) (fn [_ _ _]))}))

(defn create-action-button [data]
  (doseq [k #{:title :get-actions :prepare-canvas}]
    (when-not (k data)
      (throw (ex-info "Can't create toolbar action button without key"
                      {:k k :data data}))))
  (let [show? (or (:show? data) (constantly true))]
    (with-meta
      (dissoc data :show? :get-actions :prepare-canvas)
      {`canvas/prepare-toolbar-button
       (fn [tool state options]
         (when (show? tool state options)
           (with-meta
             {:text (when-not (:icon data) (:title data))
              :title (:title data)
              :icon (:icon data)
              :actions ((:get-actions data) tool state options)}
             {`canvas/render-toolbar-button #'MenuButton})))})))
