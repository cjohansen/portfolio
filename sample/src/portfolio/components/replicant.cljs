(ns portfolio.components.replicant
  (:require [portfolio.replicant :refer-macros [defscene]]))

(defscene standard-button
  [:button.button {:class [:another]} "I am a button"])
