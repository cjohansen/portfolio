(ns portfolio.components.rum
  (:require [rum.core :as rum]
            ;; If you are using an older version of react use the following:
            #_[portfolio.rum :refer-macros [defscene]]
            ;; For react versions 18+ use the following:
            ;; Beware newer versions of react may not work as expected rum is
            ;; not actively maintained https://github.com/tonsky/rum/issues/165
            [portfolio.react-18 :refer-macros [defscene]]))

(rum/defc button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Rum button"))
