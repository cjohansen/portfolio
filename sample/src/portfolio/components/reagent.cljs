(ns portfolio.components.reagent
  (:require [portfolio.reagent :refer-macros [defscene]]))

(defn button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Reagent button"))
