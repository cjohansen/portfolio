(ns mirror.ui.components.auto-complete-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.auto-complete :refer [AutoCompleter]]
            [portfolio.ui.icons :as icons]))

(portfolio/configure-scenes
 {:title "Auto complete"})

(defscene unadorned-input
  (AutoCompleter {:text "Apples"}))

(defscene with-placeholder
  (AutoCompleter {:placeholder "Search"}))

(defscene with-icon-and-action
  (AutoCompleter
   {:text "Apples"
    :icon ::icons/magnifying-glass
    :action {:icon ::icons/x
             :actions []}}))

(defscene placeholder-with-icon
  (AutoCompleter
   {:placeholder "Search"
    :icon ::icons/magnifying-glass}))

(defscene with-suggestions
  (AutoCompleter
   {:text "auto compl"
    :icon ::icons/magnifying-glass
    :on-input []
    :suggestions [{:title "Auto complete"
                   :illustration {:icon :portfolio.ui.icons/package
                                  :color "var(--highlight-color)"}
                   :actions []}
                  {:title "unadorned-input"
                   :description "Auto complete"
                   :illustration {:icon :portfolio.ui.icons/bookmark
                                  :color "var(--browser-unit-icon-color)"}
                   :actions []}
                  {:title "with-placeholder"
                   :description "Auto complete"
                   :illustration {:icon :portfolio.ui.icons/bookmark
                                  :color "var(--browser-unit-icon-color)"}
                   :actions []}]
    :action {:icon ::icons/x
             :actions []}}))
