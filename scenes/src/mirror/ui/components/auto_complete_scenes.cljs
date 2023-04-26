(ns mirror.ui.components.auto-complete-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.auto-complete :refer [AutoCompleter]]))

(portfolio/configure-scenes
 {:title "Auto complete"})

(defscene unadorned-input
  (AutoCompleter {:text "Apples"}))

(defscene with-placeholder
  (AutoCompleter {:placeholder "Search"}))

(defscene with-icon-and-action
  (AutoCompleter
   {:text "Apples"
    :icon (icons/icon :phosphor.regular/magnifying-glass)
    :action {:icon (icons/icon :phosphor.regular/x)
             :actions []}}))

(defscene placeholder-with-icon
  (AutoCompleter
   {:placeholder "Search"
    :icon (icons/icon :phosphor.regular/magnifying-glass)}))

(defscene with-suggestions
  (AutoCompleter
   {:text "auto compl"
    :icon (icons/icon :phosphor.regular/magnifying-glass)
    :on-input []
    :suggestions [{:title "Auto complete"
                   :illustration {:icon (icons/icon :phosphor.regular/package)
                                  :color "var(--highlight-color)"}
                   :actions []}
                  {:title "unadorned-input"
                   :description "Auto complete"
                   :illustration {:icon (icons/icon :phosphor.regular/bookmark)
                                  :color "var(--browser-unit-icon-color)"}
                   :actions []}
                  {:title "with-placeholder"
                   :description "Auto complete"
                   :illustration {:icon (icons/icon :phosphor.regular/bookmark)
                                  :color "var(--browser-unit-icon-color)"}
                   :actions []}]
    :action {:icon (icons/icon :phosphor.regular/x)
             :actions []}}))
