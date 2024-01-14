(ns portfolio.locale-selector
  (:require [portfolio.ui.canvas.addons :as addons]))

(def locales
  {:nb "Norsk"
   :en "English"})

(def default-locale :nb)

(def options
  (->> locales
       (sort-by val)
       (map (fn [[locale text]]
              {:title text
               :value {:i18n/locale locale}}))))

(defn create-locale-tool []
  (addons/create-toolbar-menu-button
   {:id :canvas/locale
    :title "Language"
    :prepare-title (fn [current-value]
                     (str "Language: " (or (get locales (:i18n/locale current-value))
                                           (get locales default-locale))))
    :default-value {:i18n/locale :nb}
    :global? true
    :persist? true
    :options options
    :prepare-canvas (fn [_ _ _])}))
