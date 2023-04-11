(ns portfolio.ui.canvas.background
  (:require [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.components.canvas :as canvas]))

(def default-options
  [{:id :light-mode
    :title "Light (.light-mode)"
    :value {:background/background-color "#fff"
            :background/body-class "light-mode"}}
   {:id :dark-mode
    :title "Dark (.dark-mode)"
    :value {:background/background-color "#111111"
            :background/body-class "dark-mode"}}])

(defn prepare-canvas [data el {:background/keys [background-color body-class]}]
  (set! (.. (canvas/get-iframe el) -style -backgroundColor) background-color)
  (let [body (canvas/get-iframe-body el)]
    (doseq [{:keys [value]} (:options data)]
      (when-not (empty? (:background/body-class value))
        (if (= body-class (:background/body-class value))
          (.add (.-classList body) (:background/body-class value))
          (.remove (.-classList body) (:background/body-class value)))))))

(defn create-background-tool [config]
  (let [options (or (:background/options config) default-options)]
    (addons/create-toolbar-menu-button
     {:id :canvas/background
      :global? true
      :persist? true
      :title "Background"
      :icon :portfolio.ui.icons/palette
      :options (or (:background/options config) options)
      :default-value (->> (or (when-let [id (:background/default-option-id config)]
                                (filter (comp #{id} :id) options))
                              options)
                          first
                          :value)
      :prepare-canvas #'prepare-canvas})))
