(ns portfolio.components.rum
  (:require [rum.core :as rum]
            [portfolio.rum :refer-macros [defscene]]))

(rum/defc button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Rum button"))
