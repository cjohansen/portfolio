(ns mirror.ui.components.status-bar-scenes
  (:require [portfolio.ui.components.status-bar :refer [StatusBar]]
            [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [phosphor.icons :as icons]))

(portfolio/configure-scenes
 {:title "StatusBar"})

(defscene all-clear
  (StatusBar
   {:icon (icons/icon :phosphor.regular/person-arms-spread)
    :label "Accessibility: 10/10"
    :statuses [{:icon (icons/icon :phosphor.regular/check-circle)
                :color :forest-green
                :label "10 passes"}]}))
