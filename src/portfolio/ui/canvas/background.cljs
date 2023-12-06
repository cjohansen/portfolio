(ns portfolio.ui.canvas.background
  (:require [phosphor.icons :as icons]
            [portfolio.ui.canvas.addons :as addons]
            [portfolio.ui.components.canvas :as canvas]))

(def default-options
  [{:id :light-mode
    :title "Light (.light-mode)"
    :value {:background/background-color "#fff"
            :background/document-class "light-mode"
            :background/body-class "light-mode"}}
   {:id :dark-mode
    :title "Dark (.dark-mode)"
    :value {:background/background-color "#111111"
            :background/document-class "dark-mode"
            :background/body-class "dark-mode"}}])

(defn prepare-canvas [data el {:background/keys [background-color body-class document-class]}]
  (set! (.. (canvas/get-iframe el) -style -backgroundColor) background-color)
  (let [body (canvas/get-iframe-body el)]
    (doseq [{:keys [value]} (:options data)]
      (when-not (empty? (:background/body-class value))
        (if (= body-class (:background/body-class value))
          (.add (.-classList body) (:background/body-class value))
          (.remove (.-classList body) (:background/body-class value))))
      (when-not (empty? (:background/document-class value))
        (if (= document-class (:background/document-class value))
          (.add (.-classList (.-parentNode body)) (:background/document-class value))
          (.remove (.-classList (.-parentNode body)) (:background/document-class value)))))))

(defn create-background-tool [config]
  (let [options (or (:background/options config) default-options)]
    (addons/create-toolbar-menu-button
     {:id :canvas/background
      :persist? true
      :title "Background"
      :icon (icons/icon :phosphor.regular/palette)
      :options (or (:background/options config) options)
      :default-value (->> (or (when-let [id (:background/default-option-id config)]
                                (filter (comp #{id} :id) options))
                              options)
                          first
                          :value)
      :prepare-canvas #'prepare-canvas})))
