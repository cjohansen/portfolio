(ns portfolio.components.helix
  (:require [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            ;; If you are using an older version of react use the following:
            #_[portfolio.react :refer-macros [defscene]]
            ;; For react versions 18+ use the following:
            [portfolio.react-18 :refer-macros [defscene]]))

(defnc counter []
  (let [[count set-count] (hooks/use-state 0)]
    (d/div
     (d/p "Count: " count)
     (d/button {:on-click #(set-count inc)} "Increase"))))

(defscene helix-counter
  :title "Counter with React Hooks"
  ($ counter))
