(ns portfolio.components.rum
  (:require [rum.core :as rum]
            ;; If you are using an older version of react use the following:
            #_[portfolio.rum :refer-macros [defscene]]
            ;; For react versions 18+ use the following:
            ;; Beware newer versions of react may not work as expected rum is
            ;; not actively maintained https://github.com/tonsky/rum/issues/165
            [portfolio.react-18 :as p-react-18 :refer-macros [defscene]]
            [portfolio.theme :as theme]))

(p-react-18/set-decorator! theme/react-18-decorator)

(rum/defc button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Rum button"))

(rum/defc decorator-consumer-component []
  (let [theme (theme/use-theme)]
    [:button {:style {:background (name theme)}}
     "current theme is " (name theme)]))


(defscene decorator-consumer-demo
  (decorator-consumer-component))