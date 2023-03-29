(ns portfolio.components.reagent
  (:require
   ;; If you are using an older version of react use the following:
   #_[portfolio.reagent :refer-macros [defscene]]
   ;; For react versions 18+ use the following:
   [portfolio.reagent-18 :refer-macros [defscene]]))

(defn button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Reagent button"))
