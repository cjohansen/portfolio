(ns portfolio.ui.canvas.addons
  (:require [cljs.reader :as r]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.canvas-toolbar-buttons :refer [MenuButton]]
            [portfolio.ui.layout :as layout]))

(defn get-tool-id [tool]
  (str (or (:group-id tool) (:id tool)) (:persist-key tool)))

(defn get-persisted-value [tool]
  (try
    (some->> (str (get-tool-id tool))
             (.getItem js/localStorage)
             not-empty
             r/read-string)
    (catch :default _e
      nil)))

(defn get-default-value [tool]
  (or (when (:persist? tool)
        (get-persisted-value tool))
      (:default-value tool)))

(defn get-global-value [state tool]
  (get-in state [:tools (get-tool-id tool) :value]))

(defn get-pane-value [state tool pane-id]
  (get-in state [:panes pane-id (get-tool-id tool) :value]))

(defn get-tool-value [state tool & [pane-id]]
  (merge (get-global-value state tool)
         (when pane-id
           (get-pane-value state tool pane-id))))

(defn get-current-value [state tool {:keys [pane-id pane-options]}]
  (or (when-let [ks (->> (:options tool)
                         (map :value)
                         (filter map?)
                         (mapcat keys)
                         seq)]
        (not-empty (select-keys pane-options ks)))
      (get-tool-value state tool pane-id)
      (get-default-value tool)))

(defn get-set-actions [state tool pane-id v]
  (let [id (get-tool-id tool)
        global? (or (:global? tool) (not (layout/split-screen? state)))]
    (cond-> [[:assoc-in [:panes pane-id id :value] v]]
      global? (conj [:assoc-in [:tools id :value] v])
      (and global? (:persist? tool)) (conj [:save-in-local-storage id v]))))

(defn get-clear-actions [state tool pane-id]
  (let [id (get-tool-id tool)
        global? (not (layout/split-screen? state))]
    (cond-> [[:dissoc-in [:panes pane-id id :value]]]
      global? (conj [:dissoc-in [:tools id :value]])
      (and global? (:persist? tool)) (conj [:save-in-local-storage id nil]))))

(defn get-expand-path [vid]
  [:canvas/tools vid :expanded])

(defn get-custom-tool-source-title [[source]]
  (case source
    :scene "Scene config"
    :collection "Collection config"
    :state-layout "Global config"
    :view "Canvas config"
    :portfolio.ui.layout/gallery-default "Default config (gallery)"
    :portfolio.ui.layout/default "Default config"))

(defn prepare-tool-menu [tool state pane]
  (let [current-value (get-current-value state tool pane)
        custom-options (when (and current-value
                                  (not (contains? (set (map :value (:options tool))) current-value)))
                         [{:title (get-custom-tool-source-title (:config-source pane))
                           :value current-value
                           :disabled? true}])]
    {:options
     (for [{:keys [title value disabled?]} (concat (:options tool) custom-options)]
       (let [selected? (= value current-value)]
         {:title title
          :selected? selected?
          :actions (when-not disabled?
                     (->> [[:dissoc-in (get-expand-path (:pane-id pane))]]
                          (concat
                           (if selected?
                             (get-clear-actions state tool (:pane-id pane))
                             (get-set-actions state tool (:pane-id pane) value)))
                          (concat
                           [(when (ifn? (:on-select tool))
                              [:fn/call (:on-select tool) value])])
                          (remove nil?)))}))}))

(defn get-tool-title [state tool pane]
  (or (when (ifn? (:prepare-title tool))
        (let [f (:prepare-title tool)]
          (f (get-current-value state tool pane))))
      (:title tool)))

(defn prepare-toolbar-menu-button [tool state pane]
  (let [expand-path (get-expand-path (:pane-id pane))
        expanded? (= (:id tool) (get-in state expand-path))]
    (with-meta
      {:text (when-not (:icon tool)
               (get-tool-title state tool pane))
       :icon (:icon tool)
       :title (when (:icon tool)
                (get-tool-title state tool pane))
       :actions (if expanded?
                  [[:dissoc-in expand-path]]
                  [[:assoc-in expand-path (:id tool)]])
       :menu (when expanded?
               (prepare-tool-menu tool state pane))}
      {`canvas/render-toolbar-button #'MenuButton})))

(defn create-toolbar-menu-button [data]
  (let [missing (filter (comp nil? data) #{:id :title :options :prepare-canvas})]
    (with-meta
      (cond-> (dissoc data :prepare-canvas)
        (seq missing)
        (assoc :problems [{:problem :missing-keys
                           :data (set missing)
                           :message "Can't create toolbar menu button without keys"}]))
      {`canvas/prepare-toolbar-button #'prepare-toolbar-menu-button
       `canvas/get-tool-value (fn [tool state pane-id] (get-tool-value state tool pane-id))
       `canvas/prepare-canvas (or (:prepare-canvas data) (fn [_ _ _]))
       `canvas/finalize-canvas (or (:finalize-canvas data) (fn [_ _ _]))})))

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
