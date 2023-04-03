(ns portfolio.components.uix
  (:require [uix.core :refer [defui $ use-state]]
            [uix.dom]
            [portfolio.react-18 :refer-macros [defscene]]))

(defui counter []
  (let [[count set-count] (use-state 0)]
    ($ :div
      ($ :p "Count: " count)
      ($ :button {:on-click #(set-count inc)} "Increase"))))

(defscene uix-counter
  :title "Counter with React Hooks"
  ($ counter))
